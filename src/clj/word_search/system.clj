(ns clj.word-search.system
  (:require [com.stuartsierra.component :as component]
            [clj.word-search.bibliotheque :as bl]
            [clj.word-search.frequency :as f]
            [clj.word-search.configuration :as c]))

(defn word-system [options]
  (component/system-map
   :configuration (c/configuration options)
   :frequency (component/using
               (f/frequency options)
               [:configuration])
   :bibliotheque (component/using
                  (bl/bibliotheque options)
                  [:configuration :frequency])))
