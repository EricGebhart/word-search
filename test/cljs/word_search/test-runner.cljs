(ns word_search.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [word_search.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'word_search.core-test))
    0
    1))
