(ns auth-client.core
  (:require [auth-client.http :as http]
            [auth-client.authentication :as authentication]
            [auth-client.retry :as retry]))

(comment
  (def token-store (header-token/token-store))
  (def authenticator (header-token/authenticator token-store))

  (def http-client
    (-> (http/client {:request-timeout 5000})
        (authentication/with authenticator)
        (retry/with retry-policy)))

  (def auth-service (let [config {:url             "https://www.google.com"
                                  :ttl             10000
                                  :response->token (fn [response] {:token (rand-int 1000)})}]
                      (auth/authentication-service http-client token-store config)))

  @(http/get http-client "https://www.google.com" {:authenticate? true})

  (stop! auth-service)

  )
