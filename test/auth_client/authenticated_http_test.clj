(ns auth-client.authentication-test
  (:require [clojure.test :refer :all]
            [auth-client.fixtures :as fixtures]
            [auth-client.http :as http]
            [auth-client.authentication :as authenticate]
            [auth-client.protocols :as proto]))

(def authenticator-called? (atom false))

(use-fixtures :once fixtures/use-fake-http-calls)
(use-fixtures :each (fn [f] (reset! authenticator-called? false) (f)))

(def authenticator
  (reify
    proto/Authenticator
    (proto/authenticated [this request]
      (reset! authenticator-called? true)
      request)))

(def http-client
  (-> (http/client)
      (authenticate/with authenticator)))

(deftest authenticator-is-not-inkoked-for-normal-calls
  @(http/get http-client "http://example.com")
  (is (false? @authenticator-called?)))

(deftest authenticator-is-inkoked-for-authenticated-calls
  @(http/get http-client "http://example.com" {:authenticate? true})
  (is (true? @authenticator-called?)))
