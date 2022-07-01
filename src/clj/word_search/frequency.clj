(ns clj.word-search.frequency
  (:require [clj.word-search.configuration :as c]
            [clj.word-search.utils :as utils]
            [clj.word-search.words :as words]
            [clj.word-search.utils :as utils]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [net.cgrand.enlive-html :as html]))

(declare load-db)
(declare save-db)
(declare fetch-freq-w-class)

(defrecord Frequency [options configuration]
  component/Lifecycle

  (start [this]
    (let [freq-db "frequency.edn"
          this (assoc this
                      :db-file freq-db)
          w (read-db this)
          save (empty? w)
          url (:list-by-type-url (c/frequency-config
                                  (:configuration this)))

          ;; if we don't have anything fetch it.
          w (or w (fetch-freq-w-class url))]

      ;; only save it if we didn't have it before.
      (when save (write-db this w))

      (assoc this
             :words w
             :url url
             :word-count (count w)
             :started true)))

  (stop [this]
    (save-db this)
    (assoc this :started nil :db-file nil :words nil)))

(defn frequency[options]
  (map->Frequency{:options options}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn save-db
  "save the library as an edn."
  [this words]
  ((utils/write-edn (:db-file this) words)))

(defn read-db [this]
  (utils/read-edn (:db-file this)))

(defn get-snippet [url]
  (html/html-snippet (slurp url)))

(defn fetch-freq-table [url]
  (partition 3
             (map html/text
                  (html/select (get-snippet url)
                               #{[:div :div :div :div :div :div :div.divTable :table :tr :td]}))))

(defn parse-int [s]
  (Integer. (re-find  #"\d+" s )))

(defn freq-word[freq word type]
  {:freq (parse-int freq)
   :word word
   :type type})

(defn map-freq-list [table]
  (map #(freq-word (first %) (second %) (last %)) table))

(defn fetch-freq-w-class [url]
  (map-freq-list (fetch-freq-table url)))

(defn list-types
  "give a unique list of word types."
  [component]
  (distinct (map :type (:words component)))
  )

(defn filter-types
  "filter by a list of types 'verbe noun preposition pronoun adverb adjective numer subst det conj'."
  [this types]
  (filter #(some #{(:type %)} types)
          (:words this)))

(defn take-words
  ([this c]
   (take c (:words this)))

  ([this c types]
   (if (or (not types)
           (empty? types))
     (take-words c)
     (take c (filter-types this types)))))

(defn range-words
  ([this s e]
   (take (- e (- s 1)) (drop (- s 1) (:words this))))

  ([this s e types]
   (if (or (not types)
           (empty? types))
     (range-words s e)
     (take (- e (- s 1)) (drop (- s 1) (filter-types this types))))))
