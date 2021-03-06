# Representations

Resources have state, but when this state needs to be transferred from
one host to another, we use one of a number of formats to represent
it. We call these formats _representations_.

A given resource may have a large number of actual or possible
representations.

Representation may differ in a number of respects, including:

- the media-type ('file format')
- if textual, the character set used to encode it into octets
- the (human) language used (if textual)
- whether and how the content is compressed

Whenever a user-agent requests the state from a resource, a particular
representation is chosen, either by the server (proactive) or client
(reactive). The process of choosing which representation is the most
suitable is known as
[_content negotiation_](/spec/rfc7231#section-3.4).

## Producing content

Content negotiation is an important feature of HTTP, allowing clients
and servers to agree on how a resource can be represented to best meet
the availability, compatibility and preferences of both parties. It is
a key factor in the survival of services over time, since both new and
legacy media-types can be supported concurrently. (It is also the
mechanism by which new versions of media-types can be introduced, even
media-types that define hypermedia interactions, more on this later.)

## Proactive negotiation

There are 2 types of content negotiation. The first is termed
_proactive negotiation_ where the server determines the type of
representation from requirements sent in the request headers. These
are the headers beginning with `Accept`.

For any resource, the available representations that can be produced
by a resource, and those that it consumers, are declared in the
__resource model__. Every resource that allows a GET method should
declare at least one representation that it is able to produce.

Let's start with a simple web-page example.

```clojure
{:produces "text/html"}
```

This is a short-hand for writing [...]

[missing text here]

```clojure
{:produces [{:media-type "text/plain"
             :language #{"en" "zh-ch"}
             :charset "UTF-8"}
            {:media-type "text/plain"
             :language "zh-ch"
             :charset "Shift_JIS;q=0.9"}]
             ```

[ todo - languages ]


```nohighlight
curl -i http://localhost:8090/hello-languages -H "Accept-Charset: Shift_JIS" -H
"Accept: text/plain" -H "Accept-Language: zh-CH"
```

```http
HTTP/1.1 200 OK
Content-Type: text/plain;charset=shift_jis
Vary: accept-charset, accept-language, accept
Server: Aleph/0.4.0
Connection: Keep-Alive
Date: Mon, 27 Jul 2015 18:38:01 GMT
Content-Length: 9

?�D���E!
```

## Reactive negotiation

The second type of negotiation is termed _reactive negotiation_ where the
agent chooses from a list of representations provided by the server.

(Currently, yada does not yet support reactive negotiation but it is
definitely on the road-map.)

## The Vary response header

[coming soon]

## Body coercion

Where necessary, and according to the semantics of the HTTP method,
yada will coerce the result of a method into an entity body of the
negotiated content type.

For example, consider the following resource-map.

```clojure
{:produces "application/json"
 :methods
 {:get
  {:response (fn [_] {:greeting "Hello"})}}}
  ```

On receiving a GET request, yada will automatically convert the map `{:greeting "Hello"}` into the JSON body `{"greeting":"Hello"}`.

However, the key phrase here is _where necessary_. If a string is returned from a method, and the content type is `application/json`, there is some ambiguity between whether the resource developer intends for yada to encode the string into JSON, or whether the string is _already_ JSON encoded. In these ambiguous cases, yada assumes
the string is JSON encoded already.

Therefore this code would produce an error when the user agent attempts to decode the JSON string.

```clojure
{:produces "application/json"
 :methods
 {:get
  {:response (fn [_] "This is not JSON")}}}
```

If you are faced with this situation, you should choose one of the following options:

- Return a map with the JSON string embedded. For example: `{:message
  "Plain old string"}`.

- Use a JSON encoder such as
  [`org.clojure/data.json`](https://github.com/clojure/data.json) or
  [Cheshire](https://github.com/dakrone/cheshire) and encode the
  string yourself.


## Consuming content

[coming soon]
