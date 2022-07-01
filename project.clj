(defproject word-search "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src" "src/clj"]
  :repl-options {:timeout 200000} ;; Defaults to 30000 (30 seconds)

  :test-paths ["test/clj"]  ;; spec/clj ???

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]  ;; newer may cause problems with piggieback.
                 [midje "1.8.3" :exclusions [environ org.clojure/core.unify]]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [org.omcljs/om "1.0.0-alpha22"]
                                        ;[prismatic/om-tools "0.3.12"]
                                        ;[secretary "1.2.3"]
                                        ;[sablono "0.3.4"]
                 [enlive "1.1.6"]
                 [org.clojure/data.json "0.2.6"]
                 [com.stuartsierra/component "0.2.3"]
                 [figwheel-sidecar "0.5.0-SNAPSHOT" :scope "test" :exclusions [http-kit]]]

  :plugins [[lein-cljsbuild "1.0.3"]
            ;[lein-environ "1.0.0"]
            ]

  ;; :env {:squiggly {:checkers [:eastwood]
  ;;                  :eastwood-exclude-linters [:unlimited-use]}}

  :min-lein-version "2.5.0"

  :uberjar-name "word-search.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:source-paths ["env/dev/clj"]
                   :test-paths ["test/clj"]

                   :dependencies [[figwheel "0.2.1-SNAPSHOT"]
                                  [figwheel-sidecar "0.2.1-SNAPSHOT"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  ;[org.clojure/tools.nrepl "0.2.10"]
                                  [weasel "0.4.2"]]

                   :repl-options {:init-ns word-search.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :plugins [[lein-figwheel "0.2.1-SNAPSHOT"]]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]}

                   :env {:is-dev true}
                   ;;       :squiggly {:checkers [:eastwood]
                   ;;                  :eastwood-exclude-linters [:unlimited-use]}}

                   :cljsbuild {:test-commands { "test" ["phantomjs" "env/test/js/unit-test.js" "env/test/unit-test.html"] }
                               :builds {:app {:source-paths ["env/dev/cljs"]}
                                        :test {:source-paths ["src/cljs" "test/cljs"]
                                               :compiler {:output-to     "resources/public/js/app_test.js"
                                                          :output-dir    "resources/public/js/test"
                                                          :source-map    "resources/public/js/test.js.map"
                                                          :preamble      ["react/react.min.js"]
                                                          :optimizations :whitespace
                                                          :pretty-print  false}}}}}

             :uberjar {:source-paths ["env/prod/clj"]
                       :hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
