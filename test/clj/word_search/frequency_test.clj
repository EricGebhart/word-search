(ns clj.word-search.frequency-test
  (:require [clj.word-search.frequency :refer :all]
            [clj.word-search.system :as sys]
            [clj.word-search.configuration :as c]
            [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]))

(defn config-system [options]
  (component/system-map
   :configuration (c/configuration options)))

;; Use a configuration system to test the helper functions that don't use the frequency component.
(def sys (component/start (config-system nil)))

(def conf (:configuration sys))

(fact "we can get a frequency list url from the system configuration"
      (:list-by-type-url (c/frequency-config conf))
      =>
      "http://eduscol.education.fr/cid47916/liste-des-mots-classee-par-frequence-decroissante.html")

(def two-words (take 2 (fetch-freq-table (:list-by-type-url (c/frequency-config conf)))))

(fact "we can fetch a word frequency table there."
      two-words => '(("1050561" "le" "(dét.)")
                     ("862100" "de" "(prép.)")))

(fact "we get our words from eduscol"
      two-words => '(("1050561" "le" "(dét.)")
                     ("862100" "de" "(prép.)")))

(fact "Turn the tuples into maps"
      (map-freq-list two-words) => '({:freq 1050561, :type "(dét.)", :word "le"}
                                     {:freq 862100, :type "(prép.)", :word "de"}))


;; start the actual full system so the config is built-in.
(def sys (component/start (sys/word-system nil)))

(def f (:frequency sys))

(fact "the frequncy component has a long list of words."
      (:word-count f) => 1499)

(fact "we can filter them by type. There are only 2 verbs in the top 20 words."
      (filter #(= (:type %) "(verbe)")  (take 20 (:words f)))
      =>
      '({:freq 351960, :type "(verbe)", :word "être"}
        {:freq 248488, :type "(verbe)", :word "avoir"}))

(fact "we can get a list of types"
      (list-types f) =>
      '("(dét.)" "(prép.)" "(verbe)"
        "(conj.)" "(pron.)" "(adv.)"
        "(adj.)" "(subst.)" "(numér.)"
        "(interj)" "(verb)" "(subst)"
        "(subs)" "(adv)"))


(fact "we can get the 2 most frequently used verbes."
      (take 2 (filter-types f ["(verbe)"])) => '({:freq 351960, :type "(verbe)", :word "être"}
                                                 {:freq 248488, :type "(verbe)", :word "avoir"}))

(fact "we can more easily get the 2 most frequent words or frequent verbs."
      (take-words f 2) => '({:freq 1050561, :type "(dét.)", :word "le"}
                            {:freq 862100, :type "(prép.)", :word "de"})

      (take-words f 2 ["(verbe)"]) => '({:freq 351960, :type "(verbe)", :word "être"}
                                        {:freq 248488, :type "(verbe)", :word "avoir"}))

(fact "we can get a range of words or words of some types like the 20-21 verbes."
      (range-words f 20 21) => '({:freq 123502, :type "(dét.)", :word "au"}
                                 {:freq 119106, :type "(dét.)", :word "de"})

      (range-words f 20 21 ["(verbe)"]) => '({:freq 14138, :type "(verbe)", :word "aimer"}
                                             {:freq 13881, :type "(verbe)", :word "croire"}))
