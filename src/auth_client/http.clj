(ns auth-client.http
  (:refer-clojure :exclude [get])
  (:require [auth-client.protocols :refer :all]
            [clojure.string :as string]
            [org.httpkit.client :as http]))

(defn- request [http-client
                {:keys [authenticate?]
                 :as   request}
               callback]
  (http/request (if authenticate?
                  (if-let [authenticator (:authenticator http-client)]
                    (authenticated authenticator request)
                    (throw (ex-info "Unable to support authenticated requests"
                                    {:reason "No authenticator was provided at construction time"})))
                  request)
                callback))

(defmacro ^:private defhttp [method]
  `(defn ~method
     ~(str "Issues an async HTTP " (string/upper-case method)
           " request using `http-client`. See `request` for details.")
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

(defrecord Client [authenticator])

(defn client [& {:keys [authenticator]}]
  (Client. authenticator))
