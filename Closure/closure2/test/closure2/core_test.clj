;; test/sieve/primes_test.clj
(ns closure2.core_test
  (:require [clojure.test :refer :all]
            [closure2.core :as sp]))

(deftest first-10-primes
  (is (= [2 3 5 7 11 13 17 19 23 29]
         (vec (take 10 sp/primes)))))

(deftest hundredth-prime
  (is (= 541 (nth sp/primes 99))))

(deftest tenthousandth-prime
  (is (= 104729 (nth sp/primes 9999))))