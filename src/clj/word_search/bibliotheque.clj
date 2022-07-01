(ns word-search.bibliotheque
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as str]
            [word-search.words :as words]))


(declare load-db)
(declare save-db)
(declare load-playlists)

(defrecord Bibliotheque [options configuration frequency]
  component/Lifecycle

  (start [this ]
    (let [default-db "words.edn"
          default-pl "playlists.edn"
          this (assoc this
                      :db-file default-db
                      :pl-file default-pl)
          words (load-db this )]
      (assoc this
             :words words
             :current-playlist {:words [] :tenses []}
             :playlists (load-playlists this )
             :word-count (count (keys words))
             :started true)))

  (stop [this ]
    (save-db this )
    (assoc this :started nil :db-file nil :words nil)))

(defn bibliotheque [options]
  (map->Bibliotheque {:options options}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-file
  "load the library edn from a file."
  [file]
  (try (read-string (slurp file))
       (catch Exception e {})))

(defn load-db
  "load the library edn from a file."
  [this ]
  (load-file (:db-file this )))

(defn load-playlists
  "load the library edn from a file."
  [this ]
  (let [pl (load-file (:pl-file this ))]
    (if (empty? pl) [] pl)))

(defn add-word
  "add a word to the library."
  [this word]
  (let [word (words/fetch-conjugasions (:configuration this) word)]
    (assoc this :words
           (assoc (:words this)
                  (keyword (:word word)) word))))

(defn add-words
  "add a list of words to the library."
  [this words]
  (map #(add-word this %) words))

(defn add-playlist
  [this playlist]
  (assoc this :playlists
         (conj (:playlists this)
               playlist)))

(defn save-db
  "save the library as an edn."
  [this]
  (spit (:db-file this) (:words this))
  (spit (:pl-file this) (:playlists this)))

(defn filtered-list
  "create a filtered list from the master word list."
  [this wordlist]
  (vals (filter #(some #{(key %)} wordlist) (:words this))))

(defn set-playlist
  "set the current words list to a list filtered by the playlist."
  [this pname]
  (let [p (first (filter #(= (:name %) pname) (:playlists this)))]
    (assoc this :current-playlist p)))

(defn random-word
  "Get a random word"
  [this]
  ((rand-nth (-> this :current-playlist :words)) (:words this)))

#_(defn conjugation-names [c]
    (map #(str/join " " (map words/vname %)) c))

(defn random-word-conjugation
  "get a random conjugation for a word according to the playlist."
  [this word]
  (words/random-word-conjugation
   word
   (get-in this [:current-playlist :tenses])))

(defn random-conjugation
  "Get a random conjugation for a random word."
  [this]
  (random-word-conjugation this
                           (random-word this)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; words is a vector of words, tenses is a list of string names from the first entry of conj-choice map.
;; playlist-conj-choices is the origin.
(defrecord Playlist [name words tenses])

(defn playlist[name words tenses]
  (map->Playlist {:name name
                  :words words
                  :tenses
                  (map #(str/join " " (map words/vname %)) tenses)}))
