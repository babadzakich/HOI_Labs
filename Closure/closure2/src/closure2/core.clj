(ns closure2.core)

(defn sieve [s]
  (lazy-seq
    (when-let [p (first s)]
      (cons p
             (lazy-seq
               (sieve
                 (remove #(zero? (mod % p)) (rest s))))))))

(def primes
  (sieve (iterate inc 2)))

(def primes
  (letfn [(is-prime? [n known-primes]
            (not-any? #(zero? (rem n %))
                      (take-while #(<= (* % %) n) known-primes)))
          (primes-step [candidates known-primes]
            (lazy-seq
              (when-let [candidate (first candidates)]
                (if (is-prime? candidate known-primes)
                  (cons candidate
                        (primes-step (rest candidates)
                                     (conj known-primes candidate)))
                  (primes-step (rest candidates) known-primes)))))]
    (primes-step (iterate inc 2) [])))