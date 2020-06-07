(defproject net.b12n/pod-mgrep "1.0.0"
  :author "Burin Choomnuan <agilecreativity@gmail.com>"
  :description "A babashka pod for interacting with multigrep"
  :url "https://github.com/agilecreativity/pod-b12n-mgrep"
  :license {:name "LGPL 3.0"
            :url  "https://www.gnu.org/licenses/lgpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [nrepl/bencode "1.1.0"]
                 [clj-commons/multigrep "0.5.0"]]
  :main pod.b12n.mgrep
  :target-path "target/%s"
  :plugins [[lein-ancient "0.6.15"]
            [io.taylorwood/lein-native-image "0.3.1"]]
  :native-image {:name "pod-b12n-mgrep"
                 :opts ["--no-server"
                        "--initialize-at-build-time"
                        "--report-unsupported-elements-at-runtime"
                        "--allow-incomplete-classpath"
                        "--enable-url-protocols=http"
                        "--enable-url-protocols=https"]}
  :global-vars {*warn-on-reflection* true}
  :profiles {:uberjar {:global-vars {*assert* false}
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]
                       :aot :all
                       :main pod.b12n.mgrep}})
