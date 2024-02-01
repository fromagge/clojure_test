(ns leinapp.first
  (:gen-class))

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))

(def invoice_items (invoice :invoice/items))

(defn xor [arg1 arg2]
  (not= arg1 arg2))

(defn get-iva
  [item]
  (->> item
       (:taxable/taxes)
       (first)
       (:tax/rate)))

(defn get-ret
  [item]
  (->> item
       (:retentionable/retentions)
       (first)
       (:retention/rate)))

;; 1. At least have one item that has :iva 19%
;; 2. At least one item has retention :ret_fuente 1%
;; 3. Every item must satisfy EXACTLY one of the above two conditions.
;;     This means that an item cannot have BOTH :iva 19% and retention :ret_fuente 1%.
(defn get_invoice_item_conditions
  [invoice_items]
  (->> invoice_items
       (filter (fn [item] (xor (= (get-iva item) 19) (= (get-ret item) 1)) ),,,)))


(get_invoice_item_conditions invoice_items)
;; => Only "ii3" and "ii4" meet the conditions