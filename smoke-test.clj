#!/usr/bin/env bb
(require '[babashka.pods :as pods])
(pods/load-pod ["pod-b12n-mgrep"])
(require '[pod.b12n.mgrep :as mgrep])

{:mgrep-project
 (mgrep/grep "defproject" "project.clj")

 :mgrep-deps
 (mgrep/grep "mvn" "deps.edn")

 ;; Replace single key in a given file
 #_(mgrep/greplace! #_(format "{{%s}}" "db-password") "my-db-password!" "config.properties")

 ;; Replace multiple keys from a given file
 #_:greplace-all! #_(mgrep/greplace-all! {:config-file "config.properties"
                                          :config-map {:db-password "<my-db-password!>"
                                                       :db-username "<my-db-username!>"}
                                          :pattern "{{%s}}"})}
