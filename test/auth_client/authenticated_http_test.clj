(ns auth-client.authenticated-http-test
  (:require [clojure.test :refer :all]
            [auth-client.fixtures :as fixtures]
            [auth-client.http :as http]
            [auth-client.authentication :as authenticate]
            [auth-client.header-token-scheme :as ht]
            [clojure.tools.logging :as log]))

(use-fixtures :once fixtures/use-fake-http-calls)

(def token "secret")

(def token-store (ht/token-store token))
(def authenticator (ht/authenticator token-store))

(def http-client (-> (http/client)
                     (authenticate/with authenticator)))

(deftest token-is-not-added-to-regular-requests
  (is (-> @(http/get http-client "http://example.com")
          (get-in [:opts :headers "Authentication"] :auth-header-not-present)
          (= :auth-header-not-present))))

(deftest token-is-added-to-authenticated-request
  (is (-> @(http/get http-client "http://example.com" {:authenticate? true})
          (get-in [:opts :headers #_"Authentication"])
          (= token))))
