(ns leinapp.third
  (:require
    [leinapp.invoice-item :as inv]))
(use 'clojure.test)

(defn
  gen_invoice
  [q p r]
  {:invoice-item/precise-quantity q :invoice-item/precise-price p :invoice-item/discount-rate r})

(deftest no_discount
  (is (= 10.0 (inv/subtotal (gen_invoice 2 5 0))))
  (is (= 0.0 (inv/subtotal (gen_invoice 0 40 0))))
  (is (= 0.0 (inv/subtotal (gen_invoice 10 0 0)))))

(deftest iva_included
  (is (= 11.89 (inv/subtotal (gen_invoice 2.0 5.0 -19.0)))) ;; ?? -> lets maybe round it - leaving this one on purpose
  (is (= 40.579 (inv/subtotal (gen_invoice 1 34.1 -19.0)))))
  (is (= 40.579 (inv/subtotal (gen_invoice 1.4 34.1 -40)))) ;; India with up to 40% tax rate!
  (is (= 0.0 (inv/subtotal (gen_invoice 1.3 0 -22.5))))
  (is (= 98.77 (inv/subtotal (gen_invoice 10 8.3 -19.0))))

(deftest with_discount
  (is (= 9.5 (inv/subtotal (gen_invoice 2 5 5))))
  (is (= 0.0 (inv/subtotal (gen_invoice 0 40 10))))
  (is (= 0.0 (inv/subtotal (gen_invoice 10 0 20))))
  (is (= 0.0 (inv/subtotal (gen_invoice 10 30 100)))))


(deftest negative_discount
  (is (= 10.5 (inv/subtotal (gen_invoice 2 5 -5))))
  (is (= 0.0 (inv/subtotal (gen_invoice 0 40 -20))))
  (is (= 0.0 (inv/subtotal (gen_invoice 10 0 -30))))
  (is (= 0.0 (inv/subtotal (gen_invoice -100 0 -30)))))

(run-tests 'leinapp.third)



