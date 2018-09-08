(ns auth-client.protocols)

(defprotocol AsyncHttpClient
  "Something capable of performing async HTTP requests"
  (request [this request callback] "See auth-client.http/request for details."))

(defprotocol Authenticator
  "Adds authentication details to a request"
  (authenticated [this request] "An authenticated `request`"))

(defprotocol SecretProvider
  "A provider of authentication secrets"
  (secret-for [this request] "The secret for authenticating `request`"))

(defprotocol SecretStore
  "A store of authentication secrets"
  (update-secrets! [this secrets] "Stores `secrets` in the store"))

(defprotocol Stoppable
  "A process that can be stopped"
  (stop! [this] "Stops the process"))
