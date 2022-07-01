(ns clj.word-search.bibliotheque-test
  (:require [clj.word-search.bibliotheque :refer :all]
            [clj.word-search.words :as words]
            [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]))



(defn word-system [options]
  (component/system-map
   :bibliotheque  (bibliotheque options)))

(def sys (component/start (word-system nil)))

(def b (:bibliotheque sys))

(fact "the library holds a lot of information"
      b => '{:started true,
             :pl-file "playlists.edn",
             :db-file "words.edn",
             :word-count 0,
             :playlists [],
             :current-playlist {:words [], :tenses []},
             :current-words {},
             :words {},
             :options nil})

(fact "Add a word"
      (keys (:words (add-word b "avoir"))) => '(:avoir))

(fact "It is ephemeral if you don't keep it."
      (keys (:words b)) => nil)

(fact "save the database with some words in it."
      (save-db (-> b
                   (add-word "avoir")
                   (add-word "coucher")
                   (add-word "trouver"))) => nil)

(fact "the words db is an edn that we can investigate."
      (keys (read-string (slurp "words.edn"))) => (contains (:avoir :coucher :trouver)
                                                            :gaps-ok :in-any-order)
      )

(def b (assoc b :words (load-db b)))

(fact "load the database"
      (keys (:words b)) => (contains (:avoir :coucher :trouver)
                                     :gaps-ok
                                     :in-any-order))

(fact "find a word by key"
      (-> b :words :avoir :word) => "avoir")

(def p (playlist "new" [:avoir :coucher] [[:Indicatif :Présent]]))

(fact "create a playlist"
      (playlist "new" [:coucher] [[:Indicatif :Présent]])
      =>
      '{:name new,
        :tenses [[:Indicatif :Présent]],
        :words [:coucher]})

(fact "the library has playlists."
      (:playlists (add-playlist b p))
      =>
      '[{:name "new",
         :tenses [[:Indicatif :Présent]],
         :words [:avoir :coucher]}])

(def b (add-playlist b p))

(fact "the component keeps playlists"
      (:playlists b) => '[{:name "new",
                           :tenses [[:Indicatif :Présent]],
                           :words [:avoir :coucher]}])

(fact "filter a word list down to a playlist"
      (map :word (filtered-list b [:avoir :coucher]))
      =>
      (contains ["coucher" "avoir"]  :in-any-order))

(fact "find a playlist"
      (first (filter #(= (:name %) "new") (:playlists b)))
      =>
      '{:name "new",
        :tenses [[:Indicatif :Présent]],
        :words [:avoir :coucher]})

(fact "set the current playlist in the library."
      (map :word (:current-words (set-playlist b "new"))) => (contains ["avoir" "coucher"]
                                                                       :in-any-order))

(def b (set-playlist b "new"))


(fact "we can get random words based on the playlist"
      (:word (random-word b)) => (fn [x] (some #(= x %)
                                              '["coucher" "avoir"])))

(fact "we can get the current tenses for the current playlist"
      (get-in b [:current-playlist :tenses]) => '[(:Indicatif :Présent)])

(fact "we can grab a word from the current words. first is the key of the word, second is the word entry"
      (:word (first (:current-words b))) => "avoir"
      (keys (first (:current-words b))) => '(:pageid :title :conjugaisions :word))

(fact "get the first word from the playlist"
      (-> b :current-playlist :words first) => :avoir)

(fact "we can get a word from words that is first word in the playlist's words."
      (let [fw (-> b :current-playlist :words first)]
        (keys (-> b :words fw))) => '(:pageid :title :conjugaisions :word)
      )

;; easiest way to get a word by key is from the master word list.
(def w (get (-> b :words)
            (-> b :current-playlist :words first)))

(fact "get a random conjugation key within the choices."
      (words/random-word-conjugation w  (-> b :current-playlist :tenses))
      => nil
      #_(fn [x] (some #(= x %)
                     '[:avoir :Indicatif :Présent])))

                                        ;(words/random-word-conjugation w [[:Indicatif :Présent]])
                                        ;(words/random-word-conjugation w nil)
                                        ;(words/word-conj-choices w)
                                        ;(conjugation-names (-> b :current-playlist :tenses))

(conjugation-names [[:Indicatif :Present] [:foo :bar]])

(nth (words/playlist-conj-choices [w]) 6)

(fact "we can get a random conjugation for a word as chosen by the playlist"
      (random-word-conjugation b w) => nil
      )

(fact "get random conjugations for the words and tenses set in the playlist"
      (random-conjugation b) => nil
      )
