(ns s3-tables-catalog-clj.core-test
  (:require [clojure.test :refer :all]
            [s3-tables-catalog-clj.core :as core]
            [cheshire.core :as json]
            [clojure.java.io :as io])
  (:import [org.apache.iceberg.catalog Namespace TableIdentifier]))

(defn parse-body [response]
  (when-let [body (:body response)]
    (-> body
        io/reader
        slurp
        (json/parse-string true))))

(defn with-mock-functions [f]
  (with-redefs [core/list-namespaces (fn [_]
                                       [{:namespace "test.namespace"
                                         :metadata {}}])
                core/create-namespace (fn [namespace _]
                                        {:namespace namespace
                                         :properties {}})
                core/list-tables (fn [_]
                                   [{:namespace "test.namespace"
                                     :name "test_table"}])]
    (f)))

(use-fixtures :each with-mock-functions)

;; Test cases
(deftest test-list-namespaces
  (testing "List namespaces endpoint"
    (let [response (core/app {:request-method :get
                              :uri "/api/v1/namespaces"
                              :query-params {}})]
      (is (= 200 (:status response)))
      (is (= [{:namespace "test.namespace"
               :metadata {}}]
             (parse-body response))))))

(deftest test-create-namespace
  (testing "Create namespace endpoint"
    (let [response (core/app {:request-method :post
                              :uri "/api/v1/namespaces"
                              :body-params {:namespace "test.namespace"}})]
      (is (= 200 (:status response)))
      (is (= {:namespace "test.namespace"
              :properties {}}
             (parse-body response))))))

(deftest test-list-tables
  (testing "List tables endpoint"
    (let [response (core/app {:request-method :get
                              :uri "/api/v1/tables"
                              :query-params {}})]
      (is (= 200 (:status response)))
      (is (= [{:namespace "test.namespace"
               :name "test_table"}]
             (parse-body response))))))
