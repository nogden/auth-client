(ns async-http.http-test
  (:require [clojure.test :refer :all]
            [async-http.fixtures :as fixtures]
            [async-http.http :as http]
            [clojure.core.async :as async]))

(use-fixtures :once fixtures/use-fake-http-calls)

(def http-client (http/client))

(deftest can-make-synchronous-http-calls-by-derefing-returned-promise
  (is (= 200 (-> @(http/get http-client "http://example.com")
                 :status))))

(deftest can-make-async-calls-with-a-callback
  (let [responses (async/chan 1)
        callback  (fn [response] (async/put! responses response))]
    (http/get http-client "http://example.com" callback)
    (is (= 200 (-> responses
                   async/<!!
                   :status)))))
