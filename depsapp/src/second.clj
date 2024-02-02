(ns second
    (:require [clojure.spec.alpha :as s]
      [clojure.data.json :as json]
      [clojure.set :as st]
      [invoice-spec :as spec])
    (:import [java.text SimpleDateFormat]))
(use 'clojure.walk)

(defn read_file [f] (json/read-str (slurp f)))

(defn val->ns
      "Add namespace to map in clojure"
      [ns data]
      (->> data
           (map (fn [[k v]] [(keyword (name ns) (name k)) v]))
           (into {})))


(defn str->date
      "Turn string to date"
      [str]
      (let [date-format (SimpleDateFormat. "dd/MM/yyyy")
            parsed (.parse date-format str)]
           parsed))

(defn update-key
      "Replacement of update func to work with thread-last operator"
      [k f v]
      (update v k f))

(defn format_customer
      "Renaming keys of customer"
      [customer]
      (st/rename-keys customer {:company_name :customer/name, :email :customer/email}))

(defn format_tax [tax]
      (->> tax
           (#(st/rename-keys % {:tax_rate :rate, :tax_category :category}))
           (val->ns :tax)
           (update-key :tax/category (constantly :iva))
           (update-key :tax/rate double)))

(defn format_taxes [taxes]
      (vec (for [tax taxes]
                (format_tax tax))))


(defn format_invoice_item
      [ii]
      (->> ii
           (val->ns :invoice-item)
           (update-key :invoice-item/taxes format_taxes)))

(defn format_invoice_items [items]
      (vec (for [ii items]
                (->> ii
                     (format_invoice_item)))))

(defn parse
      "Parse invoice json file to correct format"
      [filename]
      (let [file (read_file filename)
            m (clojure.walk/keywordize-keys file)]
           (->> m
                (:invoice)
                (val->ns :invoice)
                (update-key :invoice/customer format_customer)
                (update-key :invoice/issue_date str->date)
                (#(st/rename-keys % {:invoice/issue_date :invoice/issue-date})) ;; easiest way I found, my bad
                (update-key :invoice/items format_invoice_items))))

(def parse_data (parse "invoice.json"))
(defn run [& args] (println (s/valid? ::spec/invoice parse_data))) ;; => true ;)
