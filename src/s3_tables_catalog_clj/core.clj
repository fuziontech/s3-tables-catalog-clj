(ns s3-tables-catalog-clj.core
  (:require [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [cheshire.core :as json])
  (:import [software.amazon.s3tables.iceberg S3TablesCatalog]
           [org.apache.iceberg.catalog Namespace TableIdentifier]
           [org.apache.iceberg SchemaParser PartitionSpecParser PartitionSpec]))

(def catalog
  (doto (S3TablesCatalog.)
    (.initialize "s3-tables-catalog"
                 {"warehouse" "s3://your-bucket/warehouse"
                  "region" "us-west-2"
                  "aws.region" "us-west-2"})))

(defn get-config []
  {:catalog-version "1.5.0"
   :catalog-impl "software.amazon.s3tables.iceberg.S3TablesCatalog"
   :warehouse (.name catalog)})

(defn create-namespace [namespace-str properties]
  (let [ns (Namespace/of (into-array String (clojure.string/split namespace-str #"\.")))
        _ (.createNamespace catalog ns properties)]
    {:namespace namespace-str
     :properties properties}))

(defn list-namespaces [parent]
  (let [parent-ns (when parent
                    (Namespace/of (into-array String (clojure.string/split parent #"\."))))
        namespaces (.listNamespaces catalog parent-ns)]
    (map (fn [ns]
           {:namespace (clojure.string/join "." (.levels ns))
            :metadata (.loadNamespaceMetadata catalog ns)})
         namespaces)))

(defn load-namespace [namespace-str]
  (let [ns (Namespace/of (into-array String (clojure.string/split namespace-str #"\.")))
        metadata (.loadNamespaceMetadata catalog ns)]
    {:namespace namespace-str
     :metadata metadata}))

(defn create-table [{:keys [namespace name schema spec properties]}]
  (let [ns-parts (clojure.string/split namespace #"\.")
        identifier (TableIdentifier/of (Namespace/of (into-array String ns-parts)) name)
        iceberg-schema (SchemaParser/fromJson (json/generate-string schema))
        partition-spec (if spec
                         (PartitionSpecParser/fromJson iceberg-schema (json/generate-string spec))
                         (PartitionSpec/unpartitioned))
        table (.createTable catalog identifier iceberg-schema partition-spec (or properties {}))]
    {:namespace namespace
     :name name
     :schema (json/parse-string (SchemaParser/toJson (.schema table)) true)
     :spec (json/parse-string (PartitionSpecParser/toJson (.spec table)) true)
     :properties (.properties table)
     :location (.location table)}))

(defn list-tables [namespace]
  (let [ns (when namespace
             (Namespace/of (into-array String (clojure.string/split namespace #"\."))))
        tables (.listTables catalog ns)]
    (map (fn [table]
           {:namespace (clojure.string/join "." (.levels (.namespace table)))
            :name (.name table)})
         tables)))

(defn get-table [namespace name]
  (let [identifier (TableIdentifier/of
                    (Namespace/of (into-array String (clojure.string/split namespace #"\.")))
                    name)
        table (.loadTable catalog identifier)]
    {:namespace namespace
     :name name
     :format-version (get (.properties table) "format-version" "1")
     :location (.location table)}))

(defn drop-table [namespace name purge]
  (let [identifier (TableIdentifier/of
                    (Namespace/of (into-array String (clojure.string/split namespace #"\.")))
                    name)]
    (.dropTable catalog identifier purge)
    nil))

(def app
  (ring/ring-handler
   (ring/router
    [["/api"
      ["/v1"
       ["/config"
        {:get {:handler (fn [_] {:status 200 :body (get-config)})}}]

       ["/namespaces"
        {:get {:handler (fn [{{:keys [parent]} :query-params}]
                          {:status 200
                           :body (list-namespaces parent)})}
         :post {:handler (fn [{{:keys [namespace properties]} :body-params}]
                           {:status 200
                            :body (create-namespace namespace properties)})}}]

       ["/namespaces/:namespace"
        {:get {:handler (fn [{{:keys [namespace]} :path-params}]
                          {:status 200
                           :body (load-namespace namespace)})}}]

       ["/tables"
        {:get {:handler (fn [{{:keys [namespace]} :query-params}]
                          {:status 200
                           :body (list-tables namespace)})}
         :post {:handler (fn [{:keys [body-params]}]
                           {:status 200
                            :body (create-table body-params)})}}]

       ["/tables/:namespace/:table"
        {:get {:handler (fn [{{:keys [namespace table]} :path-params}]
                          {:status 200
                           :body (get-table namespace table)})}
         :delete {:handler (fn [{{:keys [namespace table]} :path-params
                                 {:keys [purge]} :query-params}]
                             {:status 200
                              :body (drop-table namespace table (Boolean/valueOf purge))})}}]]]]
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})))

(defn -main [& args]
  (jetty/run-jetty #'app {:port 3000
                          :join? false})
  (println "Server running on port 3000"))