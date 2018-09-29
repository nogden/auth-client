(ns async-http.retry
  (:require [async-http.protocols :as proto]))

"429 Too Many Requests"
"503 Service Unavailable"

(defn with
  "Adds a retry mechanism to `http-client` that will retry failed
  requests according to `policy`."
  [http-client {:keys [] :as policy}]
  )

(comment
  (def token-store (header-token/token-store))
  (def authenticator (header-token/authenticator token-store))

  (def http-client
    (-> (http/client {:request-timeout 5000})
        (authentication/with authenticator)
        (retry/with retry-policy)))

  (def auth-service
    (let [config {:url             "https://www.google.com"
                  :ttl             10000
                  :response->token (fn [response] {:token (rand-int 1000)})}]
      (auth/authentication-service http-client token-store config)))

  @(http/get http-client "https://www.google.com" {:authenticate? true})

  (proto/stop! auth-service)

  )
