(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 's3-tables-catalog-clj)
(def version "0.1.0-SNAPSHOT")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:direct-linking true
                                 :elide-meta [:doc :file :line :added]
                                 :disable-locals-clearing false}})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 's3-tables-catalog-clj.core
           :manifest {"Main-Class" "s3_tables_catalog_clj.core"}}))
