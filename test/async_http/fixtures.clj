(ns async-http.fixtures
  (:require [org.httpkit.fake :as fake]))

(defn use-fake-http-calls [test]
  (fake/with-fake-http [#"" 200]
    (test)))
