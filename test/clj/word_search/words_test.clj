(ns clj.word-search.words-test
  (:require [clj.word-search.words :refer :all]
            [net.cgrand.enlive-html :as html]
            [clj.word-search.wiktionary :as wiktionary]
            [clj.word-search.system :as sys]
            [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]))


(def sys (component/start (sys/word-system nil)))

(def c (:configuration sys))

(def snip (:snippet (wiktionary/parse c "avoir" :conjugation true)))

(fact "get the conjugation tables out of the content"
      (first (conjugation-entries snip)) => '{:attrs {:href "/wiki/avoir",
                                                      :title "avoir"},
                                              :content ("avoir"),
                                              :tag :a})


(facts "get some parts from the snippet."
       (let [grp1 (take-while not-group? (find-start-of-group (conjugation-entries snip)))]
         (fact "get the first group keyword"
               (html/text (first grp1))  => "Modes impersonnels")
         (fact "get the first group table"
               (show-tree-node (first (rest grp1))) => [:table])
         (fact "drop the first group and get to the next one."
               (html/text (first (drop-while not-group? (find-start-of-group (conjugation-entries snip)))))
               => "Locution verbale impersonnelle[modifier le wikicode]")))


(facts "we can create a tree of conjugaisions."
       (keys (second (first (make-master-groups (conjugation-entries snip)))))
       =>
       '(:Modes_impersonnels :Indicatif :Subjonctif :Conditionnel :Impératif))

(def avoir (fetch-conjugasions c "avoir"))

(facts "fetch-conjugasions does everything."
       (:word avoir) => "avoir"
       (:title avoir) =>  "Annexe:Conjugaison en français/avoir"
       (:pageid avoir) => 1008542
       ;(first (:entries avoir)) => '{:attrs {:href "/wiki/avoir", :title "avoir"}, :content ("avoir"), :tag :a}
       (keys (second (first (:conjugaisions avoir))))
       =>
       '(:Modes_impersonnels :Indicatif :Subjonctif :Conditionnel :Impératif)
       (first (first (:conjugaisions avoir)))=> :avoir
       (first (second (:conjugaisions avoir)))=>  :Locution_verbale_impersonnelle
       )


;; Avoir has an empty one at the end which should be eliminated.
(fact "fix the word tree so the word is at the root, avoir gets fixed, but coucher is ok to start."
      (take 31 (word-tree avoir)) =>
      '([:avoir :Modes_impersonnels :Infinitif :Passé]
        [:avoir :Modes_impersonnels :Infinitif :Présent]
        [:avoir :Modes_impersonnels :Gérondif :Passé]
        [:avoir :Modes_impersonnels :Gérondif :Présent]
        [:avoir :Modes_impersonnels :Participe :Passé]
        [:avoir :Modes_impersonnels :Participe :Présent]
        [:avoir :Indicatif :Présent]
        [:avoir :Indicatif :Passé_composé]
        [:avoir :Indicatif :Imparfait]
        [:avoir :Indicatif :Plus-que-parfait]
        [:avoir :Indicatif :Passé_simple]
        [:avoir :Indicatif :Passé_antérieur]
        [:avoir :Indicatif :Futur_simple]
        [:avoir :Indicatif :Futur_antérieur]
        [:avoir :Subjonctif :Présent]
        [:avoir :Subjonctif :Passé]
        [:avoir :Subjonctif :Imparfait]
        [:avoir :Subjonctif :Plus-que-parfait]
        [:avoir :Conditionnel :Présent]
        [:avoir :Conditionnel :Passé_1re_forme]
        [:avoir :Conditionnel :Passé_2e_forme]
        [:avoir :Impératif :Présent]
        [:avoir :Impératif :Passé]
        [:avoir :Locution_verbale_impersonnelle :Modes_impersonnels :Infinitif :Passé]
        [:avoir :Locution_verbale_impersonnelle :Modes_impersonnels :Infinitif :Présent]
        [:avoir :Locution_verbale_impersonnelle :Modes_impersonnels :Gérondif :Passé]
        [:avoir :Locution_verbale_impersonnelle :Modes_impersonnels :Gérondif :Présent]
        [:avoir :Locution_verbale_impersonnelle :Modes_impersonnels :Participe :Passé]
        [:avoir :Locution_verbale_impersonnelle :Modes_impersonnels :Participe :Présent]
        [:avoir :Locution_verbale_impersonnelle :Indicatif :Présent]
        [:avoir :Locution_verbale_impersonnelle :Indicatif :Passé_composé]
        )
      (word-tree coucher) =>
      '([:coucher :Modes_impersonnels :Infinitif :Passé]
        [:coucher :Modes_impersonnels :Infinitif :Présent]
        [:coucher :Modes_impersonnels :Gérondif :Passé]
        [:coucher :Modes_impersonnels :Gérondif :Présent]
        [:coucher :Modes_impersonnels :Participe :Passé]
        [:coucher :Modes_impersonnels :Participe :Présent]
        [:coucher :Indicatif :Présent]
        [:coucher :Indicatif :Passé_composé]
        [:coucher :Indicatif :Imparfait]
        [:coucher :Indicatif :Plus-que-parfait]
        [:coucher :Indicatif :Passé_simple]
        [:coucher :Indicatif :Passé_antérieur]
        [:coucher :Indicatif :Futur_simple]
        [:coucher :Indicatif :Futur_antérieur]
        [:coucher :Subjonctif :Présent]
        [:coucher :Subjonctif :Passé]
        [:coucher :Subjonctif :Imparfait]
        [:coucher :Subjonctif :Plus-que-parfait]
        [:coucher :Conditionnel :Présent]
        [:coucher :Conditionnel :Passé_1re_forme]
        [:coucher :Conditionnel :Passé_2e_forme]
        [:coucher :Impératif :Présent]
        [:coucher :Impératif :Passé]
        [:se_coucher :Modes_impersonnels :Infinitif :Passé]
        [:se_coucher :Modes_impersonnels :Infinitif :Présent]
        [:se_coucher :Modes_impersonnels :Gérondif :Passé]
        [:se_coucher :Modes_impersonnels :Gérondif :Présent]
        [:se_coucher :Modes_impersonnels :Participe :Passé]
        [:se_coucher :Modes_impersonnels :Participe :Présent]
        [:se_coucher :Indicatif :Présent]
        [:se_coucher :Indicatif :Passé_composé]
        [:se_coucher :Indicatif :Imparfait]
        [:se_coucher :Indicatif :Plus-que-parfait]
        [:se_coucher :Indicatif :Passé_simple]
        [:se_coucher :Indicatif :Passé_antérieur]
        [:se_coucher :Indicatif :Futur_simple]
        [:se_coucher :Indicatif :Futur_antérieur]
        [:se_coucher :Subjonctif :Présent]
        [:se_coucher :Subjonctif :Passé]
        [:se_coucher :Subjonctif :Imparfait]
        [:se_coucher :Subjonctif :Plus-que-parfait]
        [:se_coucher :Conditionnel :Présent]
        [:se_coucher :Conditionnel :Passé_1re_forme]
        [:se_coucher :Conditionnel :Passé_2e_forme]
        [:se_coucher :Impératif :Présent]
        [:se_coucher :Impératif :Passé]))

(fact "Get a distinct list of conjugation choices for the word"
      (first (word-conj-choices avoir)) => (into [(keyword "Modes_impersonnels")] [:Infinitif :Passé])
      (second (word-conj-choices avoir)) => (into [(keyword "Modes_impersonnels")] [:Infinitif :Présent])
      )

(def coucher (fetch-conjugasions c "coucher"))
(def words [avoir coucher])

(fact "Get a distinct list of conjugation choices for a list of words"
      (nth (words-conj-choices words) 6) => '(:Indicatif :Présent))

(fact "get a list of conjugation choices in string form for a menu."
      (nth (playlist-conj-choices words) 6) =>  "Indicatif Présent")

(def all-choices (word-conj-choices coucher))
(def all-choices-str (playlist-conj-choices words))
(def choices (into [] [(nth all-choices 5) (nth all-choices 7) (nth all-choices 8)]))
(def menu-choices (into [] [(nth all-choices-str 5) (nth all-choices-str 7) (nth all-choices-str 8)]))

(fact "get a list of filtered conjugation entries based on some choices."
      (filter-conj-choices avoir menu-choices)
      =>
      '{"Indicatif Imparfait" (:Indicatif :Imparfait),
        "Indicatif Passé composé" (:Indicatif :Passé composé),
        "Modes impersonnels Participe Présent" (:Modes impersonnels :Participe :Présent)})

(fact "keys in avoir"
      (name (ffirst (keys-in-tree-list avoir))) => nil)

(fact "get the word forms for a word."
      (word-forms coucher) =>  (into [:coucher] [(keyword "se coucher")])
      (word-forms avoir) =>  (into [:avoir] [(keyword "Locution verbale impersonnelle")
                                             (keyword "Verbe transitif avec adverbe de lieu")]))

(fact "get a random word form for a word."
      (random-word-form coucher) => #(or (= :coucher %) (= % (keyword "se coucher"))))

(fact "get a random conjugation key within the choices."
      (random-conjugation coucher menu-choices)
      =>
      (fn [x] (some #(= x %)
                   '[[:coucher :Indicatif :Imparfait]
                     [:coucher :Modes_impersonnels :Participe :Présent]
                     [:coucher :Indicatif :Passé_composé]
                     [:se_coucher :Indicatif :Imparfait]
                     [:se_coucher :Modes_impersonnels :Participe :Présent]
                     [:se_coucher :Indicatif :Passé_composé]])))

;; holy cow there are a lot of combinations.
;; Something is wrong with Modes impersonal - Present should not be empty.
(fact "get a random conjugation entry"
      (random-word-conjugation coucher menu-choices)
      =>
      (fn [x] (some #(= x %)
                   [[[:se_coucher :Indicatif :Passé_composé] ("vous vous êtes" "couchés" "\\vu vu.z‿ɛt ku.ʃe\\")]
                    [[:se_coucher :Indicatif :Imparfait] ("tu te" "couchais" "\\ty tə" "ku.ʃɛ\\")]
                    [[:coucher :Indicatif :Imparfait] ("ils/elles" "couchaient" "\\[il/ɛl]" "ku.ʃɛ\\")]
                    [[:coucher :Indicatif :Passé_composé] ("j’ai" "couché" "\\ʒ‿e ku.ʃe\\")]
                    [[:coucher :Indicatif :Imparfait] ("je" "couchais" "\\ʒə" "ku.ʃɛ\\")]
                    [[:coucher :Indicatif :Imparfait] ("vous" "couchiez" "\\vu" "ku.ʃje\\")]
                    [[:coucher :Indicatif :Passé_composé] ("il/elle/on a" "couché" "\\[i.l/ɛ.l/ɔ̃.n]‿a ku.ʃe\\")]


                    [[:coucher :Modes_impersonnels :Participe :Présent] ""]])))
