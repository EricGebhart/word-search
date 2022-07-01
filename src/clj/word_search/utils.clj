(ns word-search.utils
  (:require [clojure.string :as str]))

(defn simple-load-file
  "load the library edn from a file."
  [file]
  (try (vec (read-string (slurp file)))
       (catch Exception e nil)))

(defn simple-load
  "load the library edn from a file."
  [fname]
  (simple-load-file fname))

(defn simple-save
  "save the library as an edn."
  [fname edn]
  (spit fname edn))

(defn write-edn [fname edn]
  (with-open [w (clojure.java.io/writer fname)]
    (doseq [line (map str edn)]
      (.write w line)
      (.newLine w))))

(defn read-edn [fname]
  (try (with-open
         [r (clojure.java.io/reader fname)]
         (doseq [line (line-seq r)]
           line))
       (catch Exception e nil)))

(defn replace-nbsp [s]
  (str/trim (str/replace s (str (char 160)) " ")))

(defn vkeyword [w]
  (when (and w (seq w))
    (keyword (str/replace (replace-nbsp w) \  \_))))

(defn vname[w]
  (when w
    (str/replace (name w) \_ \ )))
