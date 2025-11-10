(ns cl3.core-test
  (:require [clojure.test :refer :all]
            [cl3.core :refer :all]))

;; -------------------------
;; Unit tests
;; -------------------------

(deftest pfilter-finite-correctness
  (is (= (filter odd? [1 2 3 4 5 6 7 8 9])
         (pfilter odd? [1 2 3 4 5 6 7 8 9] :chunk-size 3 :prefetch 2))))

(deftest pfilter-infinite-correctness
  ;; Take from an infinite range; it should match standard filter
  (is (= (take 100 (filter even? (range)))
         (take 100 (pfilter even? (range) :chunk-size 10 :prefetch 4)))))

(deftest pfilter-laziness-and-prefetch
  (let [counter (atom 0)
        ;; side-effecting sequence: increments counter on element realize
        s (map (fn [x] (swap! counter inc) x) (range))
        ;; predicate that only returns true for >=50
        pred (fn [x] (>= x 50))
        res (pfilter pred s :chunk-size 10 :prefetch 2)]
    ;; nothing realized yet
    (is (= 0 @counter))
    ;; force the first element of the result -> this will trigger up to
    ;; chunk-size * prefetch elements being observed (because we prefetch)
    (is (= 50 (first res)))
    ;; we should have realized at most chunk-size*prefetch elements
    (is (<= @counter (* 10 2)))))

;; -------------------------
;; Simple benchmark helper and demo
;; -------------------------
(defn time-ms
  "Return [elapsed-ms result] where elapsed-ms is how long (in ms) f took to run."
  [f]
  (let [t0 (System/nanoTime)
        res (f)
        t1 (System/nanoTime)]
    [(/ (double (- t1 t0)) 1e6) res]))

(defn demo-benchmark
  "Run a simple benchmark comparing sequential filter vs pfilter.

  The example predicate sleeps 10ms per element (to simulate expensive work).
  Tweak n, chunk-size and prefetch to see different speedups."
  []
  (let [n 200
        s (range n)
        heavy-pred (fn [x]
                     ;; simulate expensive predicate (10ms)
                     (Thread/sleep 10)
                     (even? x))
        seq-fn #(doall (filter heavy-pred s))
        pseq-fn #(doall (pfilter heavy-pred s :chunk-size 10 :prefetch 8))
        [t1 _] (time-ms seq-fn)
        [t2 _] (time-ms pseq-fn)]
    (println "Benchmark (n =" n ")")
    (println "  sequential filter time:" (format "%.1f" t1) "ms")
    (println "  parallel pfilter time: " (format "%.1f" t2) "ms")
    {:sequential-ms t1 :parallel-ms t2}))

;; optional -main for quick demo runs
(defn -main
  [& _]
  (println "Running demo benchmark...")
  (demo-benchmark)
  (println "Done."))
