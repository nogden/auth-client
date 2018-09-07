(ns auth-client.authenticated-http-test
  (:require [clojure.test :refer :all]
            [auth-client.fixtures :as fixtures]
            [auth-client.http :as http]
            [auth-client.authentication :as auth]))

(use-fixtures :once fixtures/use-fake-http-calls)

(def token "secret")

(def token-store (auth/token-store token))

(def http-client (http/client :authenticator (auth/header-token-authenticator token-store)))

(deftest token-is-not-added-to-regular-requests
  (is (-> (http/get http-client "http://example.com")
          deref
          (get-in [:opts :headers "Authentication"] :auth-header-not-present)
          (= :auth-header-not-present))))

(deftest token-is-added-to-authenticated-request
  (is (-> (http/get http-client "http://example.com" {:authenticate? true})
          deref
          (get-in [:opts :headers "Authentication"])
          (= token))))
