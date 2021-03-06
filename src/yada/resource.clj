;; Copyright © 2015, JUXT LTD.

(ns yada.resource
  (:require
   [clojure.tools.logging :refer :all]
   [manifold.deferred :as d]
   [schema.core :as s]
   [schema.coerce :as sc]
   [schema.utils :as su]
   [yada.representation :as rep]
   [yada.schema :as ys]
   yada.charset
   yada.media-type
   [yada.protocols :as p]
   [yada.util :refer [arity]])
  (:import [yada.charset CharsetMap]
           [yada.media_type MediaTypeMap]
           [java.util Date]))

;; Deprecated

(s/defschema MediaTypeSchema
  (s/either String MediaTypeMap))

(s/defschema CharsetSchema
  (s/either String CharsetMap))

(s/defschema QualifiedKeyword
  (s/both s/Keyword (s/pred namespace)))

(s/defschema MediaTypeSchemaSet
  #{MediaTypeSchema})

(s/defschema CharsetSchemaSet
  #{CharsetSchema})

(s/defschema StringSet
  #{String})

(defn as-set [x] (if (coll? x) x (set [x])))

(def +properties-coercions+
  {Date #(condp instance? %
           java.lang.Long (Date. %)
           %)
   MediaTypeSchemaSet as-set
   CharsetSchemaSet as-set
   StringSet as-set})

;; --

(defrecord Resource []
  p/ResourceCoercion
  (as-resource [this] this))

(defn resource [model]
  (let [r (ys/resource-coercer model)]
    (when (su/error? r) (throw (ex-info "Cannot turn resource-model into resource, because it doesn't conform to a resource-model schema" {:resource-model model :error (:error r)})))
    (map->Resource r)))

(extend-protocol p/ResourceCoercion
  nil
  (as-resource [_]
    (resource
     {:summary "Nil resource"
      :methods {}
      :interceptor-chain [(fn [_]
                            (throw (ex-info "" {:status 404})))]}))
  clojure.lang.Fn
  (as-resource [f]
    (let [ar (arity f)]
      (resource
       {:produces #{"text/html"
                    "text/plain"
                    "application/json"
                    "application/edn"}
        :methods
        {:* {:response (fn [ctx]
                         (case ar
                           0 (f)
                           1 (f ctx)
                           (apply f ctx (repeat (dec arity) nil)))
                         )}}}))))

