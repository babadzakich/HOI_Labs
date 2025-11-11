(ns cl3.core
  (:require [clojure.test :refer [run-tests]]))

(defn pfilter
  "Parallel filter using futures. Processes sequence in blocks.
   - pred: predicate function
   - coll: input sequence (can be infinite)
   - block-size: number of elements to process per future (default 1000)"
  ([pred coll] (pfilter pred coll 1000))
  ([pred coll block-size]
   (letfn [(process-block [block]
             ;; Process a block of elements in a future
             (future
               (doall (filter pred block))))

           (lazy-pfilter [s]
             (lazy-seq
               (when (seq s)
                 (let [block (take block-size s)
                       rest-seq (drop block-size s)
                       result-future (process-block block)]
                   ;; Concatenate the result with recursively processed rest
                   (concat @result-future (lazy-pfilter rest-seq))))))]

     (lazy-pfilter coll))))

(defn pfilter-parallel-blocks
  "Enhanced parallel filter that processes multiple blocks concurrently.
   - pred: predicate function
   - coll: input sequence
   - block-size: elements per block
   - num-blocks: number of blocks to process in parallel"
  ([pred coll] (pfilter-parallel-blocks pred coll 1000 4))
  ([pred coll block-size num-blocks]
   (letfn [(process-blocks [blocks]
             ;; Launch futures for multiple blocks
             (map (fn [block]
                    (future (doall (filter pred block))))
                  blocks))

           (lazy-pfilter [s]
             (lazy-seq
               (when (seq s)
                 (let [;; Take multiple blocks at once
                       blocks (take num-blocks
                                    (partition-all block-size s))
                       rest-seq (drop (* block-size num-blocks) s)
                       ;; Process blocks in parallel
                       futures (process-blocks blocks)
                       ;; Realize futures
                       results (mapcat deref futures)]
                   (concat results (lazy-pfilter rest-seq))))))]

     (lazy-pfilter coll))))

;; Heavy computation predicate for testing
(defn expensive-prime?
  "Check if number is prime (intentionally slow for benchmarking)"
  [n]
  (cond
    (<= n 1) false
    (<= n 3) true
    (or (zero? (mod n 2)) (zero? (mod n 3))) false
    :else (not-any? #(zero? (mod n %))
                    (range 5 (inc (Math/sqrt n)) 6))))

;; Performance Benchmarking
(defn benchmark
  "Simple benchmark function"
  [name f]
  (let [start (System/nanoTime)
        result (doall f)  ; Force evaluation
        end (System/nanoTime)
        time-ms (/ (- end start) 1000000.0)]
    (println (format "%s: %.2f ms" name time-ms))
    {:name name :time-ms time-ms :result result}))

(defn run-performance-demo
  "Demonstrate performance improvements"
  []
  (println "\n=== Performance Comparison ===\n")

  ;; Test 1: Finding primes in a range
  (println "Test 1: Finding primes between 1-10000")
  (let [data (range 1 10001)
        pred expensive-prime?]

    (benchmark "Standard filter"
               (filter pred data))

    (benchmark "Parallel filter (block=100)"
               (pfilter pred data 100))

    (benchmark "Parallel filter (block=500)"
               (pfilter pred data 500))

    (benchmark "Parallel blocks (block=250, 4 parallel)"
               (pfilter-parallel-blocks pred data 250 4)))

  ;; Test 2: Large dataset with simple predicate
  (println "\n\nTest 2: Filtering 1M elements (simple predicate)")
  (let [data (range 1000000)
        pred #(zero? (mod % 7))]

    (benchmark "Standard filter"
               (filter pred data))

    (benchmark "Parallel filter (block=10000)"
               (pfilter pred data 10000))

    (benchmark "Parallel blocks (block=5000, 8 parallel)"
               (pfilter-parallel-blocks pred data 5000 8)))

  ;; Test 3: Demonstrate laziness with infinite sequence
  (println "\n\nTest 3: Laziness - first 1000 primes from infinite sequence")
  (let [pred expensive-prime?]

    (benchmark "Standard filter (lazy)"
               (take 1000 (filter pred (range))))

    (benchmark "Parallel filter (lazy, block=500)"
               (take 1000 (pfilter pred (range) 500))))

  (println "\n=== Done ==="))

;; Main execution
(defn -main []
  (println "Running unit tests...")
  (run-tests 'cl3.core)

  (run-performance-demo))

;; Uncomment to run:
(-main)