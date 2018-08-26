(ns auth-client.core
  (:require [auth-client.authentication :as auth]
            [auth-client.http :as http]
            [auth-client.protocols :refer :all]))

(comment
  (def token-store (auth/token-store))

  (def http-client (http/client :authenticator (auth/header-token-authenticator token-store)))

  (def auth-service (let [config {:url             "https://www.google.com"
                                  :ttl             10000
                                  :response->token (fn [response] {:token (rand-int 1000)})}]
                      (auth/authentication-service http-client token-store config)))

  @(http/get http-client "https://www.google.com" {:authenticate? true})

  (stop! auth-service)

  )
