(ns auth-client.http-test
  (:require [auth-client.http :as http]
            [org.httpkit.fake :as fake]
            [clojure.test :refer :all]
            [clojure.core.async :as async]))

(defn- use-fake-http-calls [test]
  (fake/with-fake-http [#"" 200]
    (test)))

(use-fixtures :once use-fake-http-calls)

(def http-client (http/client))

(deftest can-make-synchronous-http-calls-by-derefing-returned-promise
  (is (= 200 (-> (http/get http-client "http://example.com")
                 deref
                 :status))))

(deftest can-make-async-calls-with-a-callback
  (let [responses (async/chan 1)
        callback  (fn [response] (async/put! responses response))]
    (http/get http-client "http://example.com" callback)
    (is (= 200
           (-> responses
               async/<!!
               :status)))))

(deftest throws-on-authenticated-request-with-no-authenticator
  (is (thrown? clojure.lang.ExceptionInfo
               (-> (http/get http-client "http//example.com" {:authenticate? true})))))
