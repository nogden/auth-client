(ns auth-client.header-token-scheme
  "Authentication via token in http headers"
  (:require [auth-client.protocols :refer :all]))

(defn token-store
  "A place to store authentication tokens."
  ([] (token-store nil))
  ([token]
   (let [token-store (atom token)]
     (reify
       SecretStore
       (update-secrets! [this token] (reset! token-store token))
       SecretProvider
       (secret-for [this _] @token-store)))))

(defn authenticator
  "An authenticator that will add a HTTP `Authentication` header
  to each request with a secret token."
  [token-store]
  (reify
    Authenticator
    (authenticated [this request]
      (assoc-in request [:headers "Authentication"]
                (secret-for token-store request)))))
