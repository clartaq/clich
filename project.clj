(defproject clich "0.1.1"
  :description "Clich is a demo of a simple Rich Text Editor written in ClojureScript"
  :url "https://github.com/clartaq/clich"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [reagent "0.9.1"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.19"]]

  :min-lein-version "2.5.0"

  :clean-targets ^{:protect false}

[:target-path
 [:cljsbuild :builds :app :compiler :output-dir]
 [:cljsbuild :builds :app :compiler :output-to]]

  :resource-paths ["public"]

  :figwheel {:http-server-root "."
             :nrepl-port       7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs         ["public/css"]}

  :cljsbuild {:builds {:app
                       {:source-paths ["src" "env/dev/cljs"]
                        :compiler
                                      {:main          "clich.dev"
                                       :output-to     "public/js/app.js"
                                       :output-dir    "public/js/out"
                                       :asset-path    "js/out"
                                       :source-map    true
                                       :optimizations :none
                                       :pretty-print  true}
                        :figwheel
                                      {:on-jsload "clich.core/mount-root"
                                       :open-urls ["http://localhost:3449/index.html"]}}
                       :release
                       {:source-paths ["src" "env/prod/cljs"]
                        :compiler
                                      {:output-to     "public/js/app.js"
                                       :output-dir    "public/js/release"
                                       :asset-path    "js/out"
                                       :optimizations :advanced
                                       :pretty-print  false}}}}

  :aliases {"package" ["do" "clean" ["cljsbuild" "once" "release"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.0"]
                                  [figwheel-sidecar "0.5.19"]]}})
