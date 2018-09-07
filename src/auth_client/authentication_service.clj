(ns auth-client.authentication-service
  "Tools for talking to an external authentication service"
  (:require [auth-client.http :as http]
            [auth-client.protocols :refer :all]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defrecord AuthenticationService [comms]
  Stoppable
  (stop! [this] (async/close! comms)))

(defn authentication-service
  "A client that will request new secrets from `url` using `http-client`
  every `ttl` milliseconds, and store them in `secret-store`.

  `opts` shall be passed to `http-client` when making each request and
  `response->secrets` shall be called on the response to extract the
  secrets before storing them in `secret-store`.

  `response->secrets` should yield a map with the following keys:

    `:secrets` is required and contains the value that will be passed
    to `secret-store` for storage.
    `ttl` is optional and, if provided, is the time to live for the new
    secrets, otherwise the `ttl` value in `config` is used.

  The returned client shall begin to make requests immediatly upon creation
  and can be stopped by calling `stop!`"
  [http-client
   secret-store
   {:keys [url opts response->secrets ttl] :as config}]
  (let [responses (async/chan 1)
        service   (AuthenticationService. responses)]
    (log/info "Starting authentication service client")
    (async/go-loop [timer (async/timeout 0)]
      (let [new-ttl (async/alt!
                      timer     ([_] (do (log/info "Requesting new secrets")
                                         (http/get http-client url opts #(async/put! responses %))
                                         ttl))
                      responses ([response _] (when response
                                                (try
                                                  (let [{secrets :secrets
                                                         new-ttl :ttl
                                                         :or     {new-ttl ttl}} (response->secrets response)]
                                                    (update-secrets! secret-store secrets)
                                                    (log/info "New secrets received,"
                                                              "next renewal in" new-ttl "ms")
                                                    new-ttl)
                                                  (catch Throwable e
                                                    (log/error e "Updating secrets failed")
                                                    ttl)))))]
        (if new-ttl
          (recur (async/timeout new-ttl))
          (log/info "Stopping authentication service client"))))
    service))
