(ns s3-tables-catalog-clj.mock-catalog
  (:gen-class
   :name s3_tables_catalog_clj.MockCatalog
   :extends software.amazon.s3tables.iceberg.S3TablesCatalog)
  (:import [org.apache.iceberg.catalog Namespace TableIdentifier]))

(defn -initialize [this name properties]
  nil)

(defn -listNamespaces [this parent]
  [(Namespace/of (into-array String ["test" "namespace"]))])

(defn -loadNamespaceMetadata [this namespace]
  {})

(defn -listTables [this namespace]
  [(TableIdentifier/of (into-array String ["test" "namespace" "test_table"]))])

(defn -createNamespace [this namespace properties]
  true)