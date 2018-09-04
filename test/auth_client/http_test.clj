(ns auth-client.http-test
  (:require [clojure.test :refer :all]
            [auth-client.fixtures :as fixtures]
            [auth-client.http :as http]
            [clojure.core.async :as async]))

(use-fixtures :once fixtures/use-fake-http-calls)

(def http-client (http/client))

(deftest can-make-synchronous-http-calls-by-derefing-returned-promise
  (is (= 200 (-> (http/get http-client "http://example.com")
                 deref
                 :status))))

(deftest can-make-async-calls-with-a-callback
  (let [responses (async/chan 1)
        callback  (fn [response] (async/put! responses response))]
    (http/get http-client "http://example.com" callback)
    (is (= 200 (-> responses
                   async/<!!
                   :status)))))

(deftest throws-on-authenticated-request-with-no-authenticator
  (is (thrown? clojure.lang.ExceptionInfo
               (http/get http-client "http//example.com" {:authenticate? true}))))
