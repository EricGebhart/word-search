(ns word-search.wiktionary
  (:require [word-search.configuration :as config]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as html]))


(defn query-url
  "return a url for a query, if verb is true, get the conjugations for the word."
  [c word & {:keys [conjugation] :or {conjugation nil}}]
  (let [cw (config/wiktionary-config c)]
    (str (:base-url cw) (:query-suffix cw)
         (when conjugation
           (:conjugation-prefix cw))
         word)))

(defn parse-url
  "return a url for a page parse with a page id from a query. This is where the content will come from."
  [config page-id]
  (let [cw (config/wiktionary-config config)]
    (str (:base-url cw) (:parse-suffix cw) page-id)))

(defn json-slurp
  "slurp json content from a url."
  [url]
  (json/read-str (slurp url)))

(defn page-id
  "get the page-id out of a wiktionary query result."
  [query-json]
  (ffirst (get-in query-json ["query" "pages"])))

(defn parse-snippet
  "break out the parts we care about from the wiki contents."
  [html-snippet]
  (let [p (get html-snippet "parse")
        pid (get p "pageid")
        word (get (first (get p "properties")) "*")
        title (get p "title")
        text (get-in p ["text" "*"])
        snippet (html/html-snippet text)]
    (assoc {} :word word :title title :snippet snippet :text text :pid pid)))

(defn parse
  "get a map of wiktionary content for a word or the conjugations of a word."
  [config word & {:keys [conjugation] :or {conjugation nil}}]
  (let [pid (-> (query-url config word :conjugation conjugation)
                json-slurp
                page-id)]
    (-> (parse-url config pid)
        json-slurp
        parse-snippet)))
