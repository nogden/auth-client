(ns async-http.header-token-scheme-test
  (:require [async-http.header-token-scheme :as sut]
            [clojure.test :refer :all]
            [async-http.fixtures :as fixtures]
            [async-http.http :as http]
            [async-http.authentication :as authenticate]
            [async-http.header-token-scheme :as ht]))

(use-fixtures :once fixtures/use-fake-http-calls)

(def token "secret")
(def token-store (ht/token-store token))

(def http-client
  (-> (http/client)
      (authenticate/with (ht/authenticator token-store))))

(deftest token-header-is-not-added-to-regular-requests
  (is (-> @(http/get http-client "http://example.com")
          (get-in [:opts :headers "Authentication"] :auth-header-not-present)
          (= :auth-header-not-present))))

(deftest token-header-is-added-to-authenticated-request
  (is (-> @(http/get http-client "http://example.com" {:authenticate? true})
          (get-in [:opts :headers "Authentication"])
          (= token))))
