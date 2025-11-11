(ns cl3.core-test
  (:require [clojure.test :refer :all]
            [cl3.core :refer :all]))

;; Unit Tests
(deftest test-basic-pfilter
  (testing "Basic filtering with pfilter"
    (is (= [2 4 6 8 10]
           (pfilter even? (range 1 11))))
    (is (= [1 3 5 7 9]
           (pfilter odd? (range 1 11))))
    (is (= []
           (pfilter even? [1 3 5 7 9])))))

(deftest test-pfilter-laziness
  (testing "Laziness - should not process entire infinite sequence"
    (let [counter (atom 0)
          pred (fn [x]
                 (swap! counter inc)
                 (even? x))
          result (pfilter pred (range) 10)]
      ;; Taking only 5 elements should not process all elements
      (is (= [0 2 4 6 8] (take 5 result)))
      ;; Counter should be reasonable, not millions
      (is (< @counter 100)))))

(deftest test-pfilter-with-different-block-sizes
  (testing "Different block sizes produce same results"
    (let [data (range 1 1001)
          pred even?]
      (is (= (pfilter pred data 10)
             (pfilter pred data 100)
             (pfilter pred data 500)
             (filter pred data))))))

(deftest test-parallel-blocks
  (testing "Parallel blocks filter"
    (is (= [2 4 6 8 10]
           (pfilter-parallel-blocks even? (range 1 11) 2 2)))
    (is (= (filter odd? (range 1 101))
           (pfilter-parallel-blocks odd? (range 1 101) 10 4)))))

(deftest test-empty-and-edge-cases
  (testing "Edge cases"
    (is (= [] (pfilter even? [])))
    (is (= [2] (pfilter even? [2])))
    (is (= [] (pfilter even? [1])))
    (is (= [1 2 3] (pfilter (constantly true) [1 2 3])))))