(ns clj.word-search.wiktionary-test
  (:require [clj.word-search.wiktionary :refer :all]
            [clj.word-search.system :as sys]
            [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]))


(def sys (component/start (sys/word-system nil)))

(def c (:configuration sys))

(fact "get the url to query"s
      (query-url c "avoir" :conjugation true)
      =>
      "https://fr.wiktionary.org/w/api.php?action=query&format=json&titles=Annexe:Conjugaison_en_fran%C3%A7ais/avoir")

(fact "get the json from the query"
      (keys (-> (query-url c "avoir" :conjugation true)
                json-slurp)) => '("batchcomplete" "query"))

(fact "Get the wiki page id if it exists"
      (-> (query-url c "avoir" :conjugation true)
          json-slurp
          page-id) => "1008542")

(def pid  (-> (query-url c "avoir" :conjugation true)
              json-slurp
              page-id))

(fact "get the parse url using the pageid"
      (parse-url c pid)
      =>
      "https://fr.wiktionary.org/w/api.php?action=parse&format=json&pageid=1008542")

(fact "get some json back from wiktionary. It has everything."
      (keys (get (json-slurp (parse-url c pid)) "parse"))
      =>
      (contains '("properties"
                  "sections"
                  "displaytitle"
                  "revid"
                  "pageid"
                  "categories"
                  "iwlinks"
                  "text"
                  "externallinks"
                  "links"
                  "templates"
                  "title"
                  "images"
                  "langlinks") :in-any-order))

(fact "get the pieces we care about from the json"
      (keys (parse c "avoir" :conjugation true)) => (contains '(:word :title :text :pid)
                                                              :in-any-order))

(fact "get the html snippet ready for enlive to read"
      (count (:snippet (parse c "avoir" :conjugation true))) => 8)
