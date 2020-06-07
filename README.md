# pod-b12n-mgrep

A [babashka](https://github.com/borkdude/babashka) pod for interacting with shell. 
Uses the [multigrep](https://github.com/clj-commons/multigrep) to function.

## Building prerequisites

- [Graal VM](https://www.graalvm.org/downloads/)
- [Clojure CLI](https://clojure.org/guides/getting_started)(faster) or [Leiningen](https://leiningen.org/)(better windows support)

### Building

Installing GraalVM:

- Download and extract GraalVM CE. Go to the extracted location and navigate to
  the directory where you can find bin, lib, jre and other directories.
- Run `export GRAALVM_HOME=$PWD`.

Clone the repo and from the repo directory:
- Run `$GRAALVM_HOME/bin/gu install native-image` to get the Graal native compiler.
- Run `clojure -A:native-image` if using Clojure CLI or `lein native-image` with leiningen to compile it to a native executable.
- The executable is found in `target/` if compiled via Clojure CLI or in `target/default+uberjar/` with leiningen.

## Usage via REPL

- Fire up a babashka v0.0.92+ REPL with `rlwrap bb`
- Import pods: `(require '[babashka.pods :as pods])`
- Load this pod: `(pods/load-pod ["pod-b12n-mgrep"])`. Assumes pod-b12n-sh is on the PATH.
- Load the ns: `(require '[pod.b12n.mgrep :as mgrep])`

## Usage as script 

```clojure
#!/usr/bin/env bb
(require '[babashka.pods :as pods])

;; Assumes pod-b12n-mgrep is on your PATH like ~/bin/pod-b12n-mgrep
(pods/load-pod ["pod-b12n-mgrep"])

(require '[pod.b12n.mgrep :as grep])

;; mgrep-project - basic grep 
(mgrep/grep "defproject" "project.clj")

;;greplace! - replace a single key in given file
(mgrep/greplace! "{{db-password}}
                 "<yourdb-password!>"
                  "config.properties")

;; greplace-all! - replace multiple keys in a given file 
(mgrep/greplace-all! {:config-file "/path/to/your-config.properties"
                      :config-map {:db-username "<your-db-username>"
                                   :db-password "<your-db-password>"
                                   ;; and more ..
                                   }
                            :pattern "{{%s}}")
```

## Example Output

See content of `smoke-test.clj`

```clojure
#!/usr/bin/env bb
(require '[babashka.pods :as pods])
(pods/load-pod ["pod-b12n-mgrep"])
(require '[pod.b12n.mgrep :as mgrep])

{:mgrep-project
 (mgrep/grep "defproject" "project.clj")

 :mgrep-deps
 (mgrep/grep "mvn" "deps.edn")}
```

Output of `./smoke-test.clj`

```clojure
{:mgrep-project "({:file \"project.clj\", :line \"(defproject net.b12n/pod-mgrep \\\"1.0.0\\\"\", :line-number 1, :regex #\"defproject\", :re-seq (\"defproject\")})", :mgrep-deps "({:file \"deps.edn\", :line \"{:deps {org.clojure/clojure   {:mvn/version \\\"1.10.1\\\"}\", :line-number 1, :regex #\"mvn\", :re-seq (\"mvn\")} {:file \"deps.edn\", :line \"        nrepl/bencode         {:mvn/version \\\"1.1.0\\\"}\", :line-number 2, :regex #\"mvn\", :re-seq (\"mvn\")} {:file \"deps.edn\", :line \"        clj-commons/multigrep {:mvn/version \\\"0.5.0\\\"}}\", :line-number 3, :regex #\"mvn\", :re-seq (\"mvn\")})"}
```

- `mgrep/greplace!` [TODO: add example output] 

- `mgrep/greplace-all!` - [TODO: add example output]
