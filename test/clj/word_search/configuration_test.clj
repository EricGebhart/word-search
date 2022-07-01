(ns clj.word-search.configuration-test
  (:require [clj.word-search.configuration :refer :all]
            [clj.word-search.system :as sys]
            [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]))


(defn word-system [options]
  (component/system-map
   :configuration (configuration options)))

(def sys (component/start (word-system nil)))

(def c (:configuration sys))

(fact "configuration knows stuff"
      (keys c) => '(:db-file :options :langs :current-language :started))

(fact "it should at least have french"
      (keys (:langs c)) => '(:française))

(fact "we can get the current-lang"
      (:current-language c) => :française)

(fact "we can get the current language's configuration"
      (language-config c)
      =>
      '{:abbrev "fr",
        :frequency {:50k-parent-page "https://invokeit.wordpress.com/frequency-word-lists/",
                    :list-by-type-url "http://eduscol.education.fr/cid47916/liste-des-mots-classee-par-frequence-decroissante.html",
                    :sub-titles-freq-file "resources/fr.txt"},
        :wiktionary {:base-url "https://fr.wiktionary.org/w/api.php?action=",
                     :conjugation-prefix "Annexe:Conjugaison_en_fran%C3%A7ais/",
                     :parse-suffix "parse&format=json&pageid=",
                     :query-suffix "query&format=json&titles="}})

(fact "we can get the current language's wiktionary configuration"
      (wiktionary-config c) =>  '{:base-url "https://fr.wiktionary.org/w/api.php?action=",
                                  :conjugation-prefix "Annexe:Conjugaison_en_fran%C3%A7ais/",
                                  :parse-suffix "parse&format=json&pageid=",
                                  :query-suffix "query&format=json&titles="})

(fact "we can get the current language's frequency list configuration"
      (frequency-config c)
      =>
      '{:50k-parent-page "https://invokeit.wordpress.com/frequency-word-lists/",
        :list-by-type-url "http://eduscol.education.fr/cid47916/liste-des-mots-classee-par-frequence-decroissante.html",
        :sub-titles-freq-file "resources/fr.txt"})

(fact "we can get the path to where the language data should be stored."
      (language-path c) => "data/française")
