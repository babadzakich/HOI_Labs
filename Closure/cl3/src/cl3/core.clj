(ns cl3.core
  "Lazy parallel filter: each future handles a block (chunk) of elements.

  Public API:
    (pfilter pred coll & {:keys [chunk-size prefetch] :or {chunk-size 1000 prefetch 4}})

  Notes:
   - The returned sequence is lazy, but pfilter will start up to `prefetch`
     futures (each handling `chunk-size` elements) ahead of consumption.
   - Tune chunk-size and prefetch for your workload and CPU count.
   - Side-effects in the predicate will be observed for elements processed
     by prefetched chunks."
  (:require [clojure.test :refer :all])
  (:gen-class))

;; -------------------------
;; Helpers
;; -------------------------

(defn chunked-lazy
  "Return a lazy sequence of vectors each of size up-to n from sequence s.
  Each chunk is realized (doall) so that it can be processed safely in a future."
  [n s]
  (lazy-seq
    (when-let [s (seq s)]
      (let [chunk (doall (take n s))]
        (cons chunk (chunked-lazy n (drop n s)))))))

;; -------------------------
;; pfilter implementation
;; -------------------------

(defn pfilter
  "Parallel lazy filter.

  pred     - predicate to test each element
  s        - input (possibly infinite) sequence
  options:
    :chunk-size (default 1000) - how many elements each future should process
    :prefetch   (default 4)    - how many futures to keep started ahead

  Returns a lazy sequence of elements from s that satisfy pred.
  Each future evaluates (doall (filter pred chunk)) for a chunk."
  [pred s & {:keys [chunk-size prefetch]
             :or   {chunk-size 1000 prefetch 4}}]
  (let [chunks (chunked-lazy chunk-size s)]
    (letfn [(start-n-futures
              [n chs]
              ;; start up to n futures, returning [vector-of-futures remaining-chunks-seq]
              (loop [i n chs chs acc []]
                (if (and (pos? i) (seq chs))
                  (recur (dec i) (rest chs)
                         (conj acc (future (doall (filter pred (first chs))))))
                  [acc chs])))]
      (let [[initial-futs rem-chunks] (start-n-futures prefetch chunks)]
        (letfn [(emit
                  [futs rem-chs]
                  (lazy-seq
                    (when-let [futsq (seq futs)]
                      (let [f (first futsq)
                            ;; keep the prefetch buffer full by starting one more future (if possible)
                            new-fut (when (seq rem-chs)
                                      (future (doall (filter pred (first rem-chs)))))
                            new-futs (cond-> (rest futsq) new-fut (conj new-fut))
                            new-rem  (if (seq rem-chs) (rest rem-chs) nil)
                            res      @f]                      ;; block until this chunk's future is done
                        ;; concatenate the filtered chunk's results with the rest (still lazy)
                        (concat res (emit new-futs new-rem))))))]
          (emit initial-futs rem-chunks))))))

