(ns word-search.words
  (:require [net.cgrand.enlive-html :as html]
            [word-search.wiktionary :as wiktionary]
            [clojure.string :as str]
            [word-search.utils :as utils]
            [clojure.data.json :as json]))

(defn is-h2? [node]
  (= :h2 (:tag node)))

(defn is-a? [node]
  (= :a (:tag node)))

(defn nth-keyword [nodes n]
  (utils/vkeyword (html/text (nth nodes n))))

(defn node-keyword [nodes]
  (if (= :h2 (:tag (first nodes)))
    (utils/vkeyword (html/text (first (:content (first nodes)))))
    (utils/vkeyword (html/text (first nodes)))))

(defn get-table [table-node] (str/split (html/text table-node) #"\n"))

(defn get-table-name [table]
  (first (remove str/blank? table)))

;;(html/select snippet #{[:div :div :table :td :table] [:h3 :span.mw-headline]})
(defn conjugation-entries [snippet]
  (html/select snippet #{[:div :div :table :td :table]
                         [:div#mb0og1 :div [:table html/first-of-type] ]
                         [:div#mb0og2 :div [:table html/first-of-type] ]
                         [:h3 :span.mw-headline]
                         [:p :b :a]  ;; conjugaison verb. title=avoir, coucher, se coucher.
                         [:h2]}))

(defn get-rows [table]
  (let [res []
        row (map utils/replace-nbsp (take-while #(not (str/blank? %)) table))
        rest-table (drop-while #(str/blank? %) (drop-while #(not (str/blank? %)) table))]
    (if (empty? rest-table)
      [row]
      (into [row] (get-rows rest-table)))))

(defn map-modes [row grp1 grp2]
  (let [mode (utils/vkeyword (first row))
        parts (partition 3 (rest row))]
    (assoc {} mode (assoc {} grp1 (first parts) grp2 (second parts)))))

(defn get-modes [nodes]
  (let [table (remove empty? (get-rows (get-table nodes)))
        grp1  (utils/vkeyword (second (first table)))
        grp2  (utils/vkeyword (last (first table)))
        rows  (rest table)
        tree  (into {} (map #(map-modes % grp1 grp2) rows))]
    tree))

(defn get-group [nodes]
  (if (= :span (:tag (second nodes)))
    [(node-keyword nodes) (nth-keyword nodes 2) (rest (rest nodes))]
    ["" (node-keyword nodes) (rest nodes)]))

(defn find-rows [table tname]
  (drop-while #(or (str/blank? %) (= tname %)) table))

(defn map-tense [table-node]
  (let [table (get-table table-node)
        table-name (get-table-name table)
        rows (get-rows (find-rows table table-name))]
    {(utils/vkeyword table-name) rows}))

(defn group-tables [nodes]
  (let [grp (utils/vkeyword (html/text (first nodes)))
        r (rest nodes)
        tbls (into {} (map map-tense (take-while #(= :table (:tag %)) r)))
        tbl-grp (assoc {} grp tbls)
        rest-nodes  (drop-while #(= :table (:tag %)) r)]
    (if (empty? rest-nodes)
      tbl-grp
      (into tbl-grp (group-tables rest-nodes)))))

(defn create-tables [nodes]
  (let [k (node-keyword nodes)
        first-tbl (get-modes (second nodes))
        r (rest (rest nodes))
        tbl-grp (group-tables r)]
    (when k
      (into (assoc {} k first-tbl) tbl-grp))))

(defn find-start-of-group [nodes]
  (if (= :a (:tag (first nodes)))
    (rest nodes)
    (rest nodes)))

(defn group? [node]
  (or (is-a? node)
      (is-h2? node)))

(defn not-group? [node]
  (not (group? node)))

(defn make-master-groups [nodes]
  (let [mkey (node-keyword nodes)
        r (find-start-of-group nodes)
        nodes (create-tables (take-while not-group? r))
        mgrp (when nodes (assoc {} mkey nodes))
        rnodes (drop-while not-group? r)]
    (if (empty? rnodes)
      [mgrp]
      (into mgrp (make-master-groups rnodes)))))

;;[:div :div :table (html/nth-of-type 1)]
;;(def hsnippet (html/html-snippet (:text (wiki-snippet-attrs (wiki-conj-snippet-for "avoir")))))
;;(def grp1 (take-while not-group? (find-start-of-group (conjugation-entries hsnippet))))
;;(def grp1-key (utils/vkeyword (html/text (first grp1))))
;;(def grp2 (drop-while not-group? (find-start-of-group (conjugation-entries hsnippet))))

(defn show-tree-node [node]
  (if (= (:tag node) :table)
    [(:tag node)]
    [(:tag node) (:content node)]))

(defn show-tree-nodes [word]
  (map show-tree-node (:entries word)))

(defn fetch-conjugasions [c word]
  (let [{:keys [pid snippet word title]} (wiktionary/parse c word :conjugation true)
        conjugaisions  (make-master-groups  (conjugation-entries snippet))]
    {:word word
     :title title
     :pageid pid
     :conjugaisions conjugaisions}))

(defn keys-in-tree-list [m]
  (if (map? m)
    (vec
     (mapcat (fn [[k v]]
               (let [sub (keys-in-tree-list v)
                     nested (map #(into [k] %) (filter (comp not empty?) sub))]
                 (if (seq nested)
                   nested
                   [[k]])))
             m))
    []))

(defn fix-keys [k v]
  (if (re-find (re-pattern k) (name (first v)))
    v
    (into [(utils/vkeyword k)] v)))

(defn word-tree [word]
  ;; basically make sure that the verbe key is at the front of all the keys.
  ;; coucher and se coucher are untouched.  Avoir and Locution verbale, are.
  (let [k (keys-in-tree-list (:conjugaisions word))
        key1 (name (ffirst k))]
    (map #(fix-keys key1 %) k)))

(defn word-conj-choices [word]
  "give a distinct list of conjugaisions for this word."
  (distinct (map rest (word-tree word))))

(defn words-conj-choices [words]
  "give a string keyed list of possible conjugation tables for a list of words"
  (distinct (mapcat word-conj-choices words)))

(defn conj-choice-map [c]
  (map #(assoc {} (str/join " " (map utils/vname %)) %) c))

(defn conj-choices [ccm]
  "this is for playlist tenses."
  (map #(first (keys %)) ccm))

(defn playlist-conj-choices [words]
  "give a full list of conjugation choices for a set of words"
  (conj-choices (conj-choice-map (words-conj-choices words))))

(defn filter-conj-choices [word choices]
  "give a filtered list of word conjugation choices based on a list of choice names."
  (let [cm (conj-choice-map (word-conj-choices word))]
    (if (empty? choices)
      cm
      (filter #(contains? (set choices) (first (keys %))) cm))))

(defn word-forms [word]
  "return the word forms for this verb. coucher, se coucher, avoir, etc."
  (keys (:conjugaisions word)))

(defn random-word-form [word]
  (rand-nth (word-forms word)))

(defn random-conjugation [word choices]
  (->> (filter-conj-choices word choices)
       rand-nth
       vals
       first
       (into [(random-word-form word)])))

(defn random-word-conjugation [word choices]
  (let [keymap (random-conjugation word choices)]
    [keymap (rand-nth (get-in (:conjugaisions word) keymap))]))


#_(filter #(re-find
            (re-pattern (name (ffirst (keys-in-tree-list (:conjugaisions avoir)))))
            (name (first %)))
          (keys-in-tree-list (:conjugaisions avoir)))


;;(def avoir (fetch-conjugasions "avoir"))
;;(def coucher (fetch-conjugasions "coucher"))
;;(def snippet (:snippet avoir))
;;(def ce (conjugation-entries (:snippet coucher)))
;;(def mg (make-master-groups ce))
