(ns auth-client.authentication
  "Construction functions for authentication related objects."
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [auth-client.protocols :refer [Authenticator Stoppable]]
            [auth-client.http :as http]))

(defn token-store
  "A place to store authentication tokens."
  ([] (token-store nil))
  ([token] (atom token)))

(defn header-token-authenticator
  "An authenticator that will add an authentication token as a header in
  each request."
  [token-store]
  (reify
    Authenticator
    (authenticated [this request]
      (assoc-in request [:headers "Authentication"] @token-store))))

(defrecord AuthenticationService [comms]
  Stoppable
  (stop! [this] (async/close! comms)))

(defn authentication-service
  [http-client
   token-store
   {:keys [url opts response->token ttl] :as config}]
  (let [responses (async/chan 1)
        service (AuthenticationService. responses)]
    (log/info "Starting authentication service client")
    (async/go-loop [timer (async/timeout 0)]
      (let [new-ttl (async/alt!
                      timer     ([_] (do (log/info "Requesting new auth token")
                                         (http/get http-client url opts #(async/put! responses %))
                                         ttl))
                      responses ([response _] (when response
                                                (try
                                                  (let [{token   :token
                                                         new-ttl :ttl
                                                         :or     {new-ttl ttl}} (response->token response)]
                                                    (reset! token-store token)
                                                    (log/info "New auth token received,"
                                                              "next renewal in" new-ttl "ms")
                                                    new-ttl)
                                                  (catch Throwable e
                                                    (log/error e "Updating auth token failed")
                                                    ttl)))))]
        (if new-ttl
          (recur (async/timeout new-ttl))
          (log/info "Stopping authentication service client"))))
    service))
