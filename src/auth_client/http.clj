(ns auth-client.http
  "An extensible, asynchronous HTTP client"
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as string]
            [org.httpkit.client :as http]
            [auth-client.protocols :as proto]))

(defn request
  "Asyncronously issues `request`, using `http-client` and returns a
  promise that will be fulfilled with the response.  If specified,
  `callback` will also be called with the response when it arrives."
  [http-client request callback]
  (http/request request callback))

(defn client
  "Create a new asynchronous HTTP client that will use `default-opts`
  for all requests in which overrides are not specified."
  ([] (client {}))
  ([{:keys [] :as default-opts}]
   (reify
     proto/AsyncHttpClient
     (proto/request [this request callback]
       (http/request (merge default-opts request) callback)))))

(defmacro ^:private defhttp [method]
  `(defn ~method
     ~(str "Issues an asynchronous HTTP " (string/upper-case method)
           " request to `url` using `http-client`.\n  Extra options may be "
           " provided in `opts`, see `request` for details.")
     ~'{:arglists '([http-client url & [opts callback]]
                    [http-client url & [callback]])}
     ~'[http-client url & [s1 s2]]
     (if (or (instance? clojure.lang.MultiFn ~'s1) (fn? ~'s1) (keyword? ~'s1))
       (request ~'http-client {:url ~'url :method ~(keyword method)} ~'s1)
       (request ~'http-client (merge ~'s1 {:url ~'url :method ~(keyword method)}) ~'s2))))

(defhttp get)
(defhttp put)
(defhttp post)
(defhttp head)
(defhttp patch)
(defhttp delete)
(defhttp options)
