(ns leinapp.first
  (:gen-class))

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))

(def invoice_items (invoice :invoice/items))

(defn xor [arg1 arg2]
  (not= arg1 arg2))

(def iva_rate 19)
(def retention_rate 1)
(defn has_iva? [{:taxable/keys [taxes]}]
  (some #(= iva_rate (:tax/rate %)) taxes))

(defn has_ret? [{:retentionable/keys [retentions]}]
  (some #(= retention_rate (:retention/rate %)) retentions))


;; 1. At least have one item that has :iva 19%
;; 2. At least one item has retention :ret_fuente 1%
;; 3. Every item must satisfy EXACTLY one of the above two conditions.
;;     This means that an item cannot have BOTH :iva 19% and retention :ret_fuente 1%.
(defn get_invoice_item_conditions
  [invoice_items]
  (->> invoice_items
       (filter (fn [item] (xor (has_iva? item) (has_ret? item))))))


(get_invoice_item_conditions invoice_items)
;; => Only "ii3" and "ii4" meet the conditions