(ns async-http.authenticated-http-test
  (:require [clojure.test :refer :all]
            [async-http.fixtures :as fixtures]
            [async-http.http :as http]
            [async-http.authentication :as authenticate]
            [async-http.protocols :as proto]))

(def authenticator-called? (atom false))

(use-fixtures :once fixtures/use-fake-http-calls)
(use-fixtures :each (fn [f] (reset! authenticator-called? false) (f)))

(def authenticator
  (reify proto/Authenticator
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
