(ns pod.b12n.mgrep
  (:refer-clojure :exclude [read read-string])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [bencode.core :as bencode]
            [multigrep.core :as mgrep])
  (:import [java.io PushbackInputStream]
           [java.io EOFException])
  (:gen-class))

(def debug? false)

(defn debug
  [& args]
  (when debug?
    (binding [*out* (io/writer "debug.log" :append true)]
      (apply println args))))

(def stdin (PushbackInputStream. System/in))

(defn write
  [v]
  (bencode/write-bencode System/out v)
  (.flush System/out))

(defn read-string
  [^"[B" v]
  (String. v))

(defn read
  []
  (bencode/read-bencode stdin))

(defn greplace-all!
  "Perform search and replace a list of keys/value in a given file.

  ;; a) Use string as keys for config-map
  (greplace-all!
    {:config-file \"/path/to/your/config-dev.properties.example\"
     :config-map {\"web-db-password\"    \"secret-pass\"
                  \"vendor-db-password\" \"more-secrets\"}
     :pattern \"{{%s}}\"
    })

  ;; b) Use keyword as keys for config-map
  (greplace-all!
    {:config-file \"/path/to/your/config-dev.properties.example\"
     :config-map {:web-db-password    \"secret-pass\"
                  :vendor-db-password \"more-secrets\"}
     :pattern \"{{%s}}\"})

  ;; c) Use symbol as keys for config-map
  (greplace-all!
    {:config-file \"/path/to/your/config-dev.properties.example\"
     :config-map {:web-db-password    \"secret-pass\"
                  :vendor-db-password \"more-secrets\"}
     :pattern \"{{%s}}\"})"
  [& [{:keys [config-file
              config-map
              pattern]
       :or {pattern "{{%s}}"}}]]
  (->
   (for [[k v] config-map]
     (if-let [r (seq (mgrep/greplace! (format pattern (name k)) v
                                      config-file))]
       [k r]))
   pr-str))

(defn grep
  [pattern file]
  (-> (mgrep/grep (re-pattern pattern) file)
      pr-str))

(def lookup
  {'pod.b12n.mgrep/greplace!      multigrep.core/greplace!
   'pod.b12n.mgrep/greplace-all!  greplace-all!
   'pod.b12n.mgrep/grep           grep})

(defn -main
  [& _args]
  (loop []
    (let [message (try
                    (read)
                    (catch EOFException _ ::EOF))]
      (when-not (identical? ::EOF message)
        (let [op (-> message
                     (get "op")
                     read-string
                     keyword)
              id (some-> (get message "id")
                         read-string)
              id (or id "unknown")]
          (case op
            :describe (do
                        (write {"format" "edn"
                                "readers" {"object" "clojure.core/str"}
                                "namespaces"
                                [{"name" "pod.b12n.mgrep"
                                  "vars" [{"name" "greplace!"}
                                          {"name" "greplace-all!"}
                                          {"name" "grep"}]}]
                                "id"  id
                                "ops" {"shutdown" {}}})
                        (recur))
            :invoke   (do
                        (try
                          (let [var  (-> message
                                         (get "var")
                                         read-string
                                         symbol)
                                args (-> message
                                         (get "args")
                                         read-string
                                         edn/read-string)]
                            (if-let [f (lookup var)]
                              (let [value (binding [*print-meta* true]
                                            (let [result (apply f args)]
                                              (pr-str result)))
                                    reply {"value"  value
                                           "id"     id
                                           "status" ["done"]}]
                                (write reply))
                              (throw (ex-info (str "Var not found: " var) {}))))
                          (catch Throwable e
                            (binding [*out* *err*]
                              (println e))
                            (let [reply {"ex-message" (ex-message e)
                                         "ex-data"    (pr-str (assoc (ex-data e) :type (class e)))
                                         "id"         id
                                         "status"     ["done" "error"]}]
                              (write reply))))
                        (recur))
            :shutdown (System/exit 0)
            (do
              (let [reply {"ex-message" "Unknown op"
                           "ex-data" (pr-str {:op op})
                           "id" id
                           "status" ["done" "error"]}]
                (write reply))
              (recur))))))))
