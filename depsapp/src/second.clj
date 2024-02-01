(ns second
  (:require [clojure.spec.alpha :as s]
            [clojure.data.json :as json]
            [clojure.set :as st]
            [invoice-spec :as spec])
  (:import [java.text SimpleDateFormat]))

(defn read_file [f] (json/read-str (slurp f)))

(defn str->date [str]
  (let [date-format (SimpleDateFormat. "dd/MM/yyyy")
        parsed (.parse date-format str)]
    parsed))

(defn str->ns-keyword
  "Turns all keys in the map into a namespace keyword"
  [_map ns]
  (cond
    (map? _map) (into {}
                      (for [[k v] _map]
                        [(keyword ns k) (str->ns-keyword v nil)]))
    (vector? _map) (vector (map (if (nil? ns) #(str->ns-keyword % nil) #(str->ns-keyword % ns)) _map))
    :else _map))

(defn format_customer
  [customer]
  (st/rename-keys customer {"company_name" :customer/name, "email" :customer/email}))

(defn format_tax [t]
  (identity {:tax/category (keyword (clojure.string/lower-case (t :tax_category))) :tax/rate (double (t :tax_rate))}))


(defn format_taxes [t]
  (let [taxes (first t)]
    (vec (map format_tax taxes))))


(defn format_items [items]
  (let [formatted (map #(str->ns-keyword % "invoice-item") items)]
    (vec (map #(assoc % :invoice-item/taxes (format_taxes (:invoice-item/taxes %))) formatted))))


(defn parse
  "Parse invoice json file to correct format"
  [filename]
  (let [raw ((read_file filename) "invoice")
        w_date (assoc {} :invoice/issue-date (str->date (raw "issue_date")))
        w_customer (assoc w_date :invoice/customer (format_customer (raw "customer")))
        w_items (assoc w_customer :invoice/items (format_items (raw "items")))]
    (identity w_items)))


(def parse_data (parse "invoice.json"))

(defn run [& args] (println (s/valid? ::spec/invoice parse_data))) ;; => true ;)

