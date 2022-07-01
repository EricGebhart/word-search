(ns word-search.configuration
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as str]))

(declare load-db)
(declare save-db)

(def simple-config
  {:current-language :française
   :langs {:française
           {:abbrev "fr"
            :wiktionary
            {:base-url "https://fr.wiktionary.org/w/api.php?action="
             :conjugation-prefix "Annexe:Conjugaison_en_fran%C3%A7ais/"
             :query-suffix "query&format=json&titles="
             :parse-suffix "parse&format=json&pageid="
             }
            :frequency
            {:50k-parent-page "https://invokeit.wordpress.com/frequency-word-lists/"
             :sub-titles-freq-file "resources/fr.txt"
             :list-by-type-url
             "http://eduscol.education.fr/cid47916/liste-des-mots-classee-par-frequence-decroissante.html"}
            }}
   })

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Configuration [db-file]
  component/Lifecycle

  (start [this]
    (let [config-db "configuration.edn"
          this (assoc this
                      :db-file config-db)
          f (load-db this )
          f (if (empty? f) simple-config f)
          langs (:langs f)
          current-language (:current-language f)]

      (assoc this
             :langs langs
             :current-language current-language
             :started true)))

  (stop [this ]
    (save-db this )
    (assoc this :started nil :db-file nil :current-language nil :langs nil)))

(defn configuration[options]
  (map->Configuration{:options options}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-file
  "load the library edn from a file."
  [file]
  (try (read-string (slurp file))
       (catch Exception e [])))

(defn load-db
  "load the library edn from a file."
  [this ]
  (load-file (:db-file this )))

(defn save-db
  "save the library as an edn."
  [this words]
  (spit (:db-file this )
        (str {:current-language (:current-language this )
              :langs (:langs this )})))

(defn language-config [c]
  (get-in c [:langs (:current-language c)]) )

(defn wiktionary-config [c]
  (:wiktionary (language-config c)))

(defn frequency-config [c]
  (:frequency (language-config c)))

(defn language-path [c]
  (str "data/" (name (:current-language c))))

(defn ensure-language-path [c]
  (when (not (.exists (language-path c)))
    (.mkdirs (language-path c))))
