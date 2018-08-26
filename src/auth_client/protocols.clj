(ns auth-client.protocols)

(defprotocol Authenticator
  "Adds authentication details to a request"
  (authenticated [this request] "An authenticated `request`"))

(defprotocol Stoppable
  "A process that can be stopped"
  (stop! [this] "Stops the process"))
