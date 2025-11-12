(ns c5.core)
(def restart_cnt (atom 0))
(defn cycle [left_fork right_fork]
  (dosync
    (swap! restart_cnt inc)
    (alter left_fork inc)
    (alter right_fork inc)
    (Thread/sleep 1000)
    )
  (Thread/sleep 1000)
  )

(defn philosopher_cycle [left_fork right_fork]
  (try (cycle left_fork right_fork) (catch Exception e)))

(defn philosopher
  [left_fork right_fork]
  (new Thread
       (fn []
         (philosopher_cycle left_fork right_fork)
         (recur)
         )
       )
  )

(defn start_philosophers
  [fork_refs]
  ;(println fork_refs)
  (doall (for [x (range (count fork_refs))]
           (.start (philosopher (nth fork_refs x) (nth fork_refs (rem (+ x 1) (count fork_refs)))))
           ))
  )

(defn make_ref
  [n]
  (vec (repeatedly n #(ref 0))))

(def forks (make_ref 4))
(start_philosophers forks)
(.start (Thread. (fn []
                   (Thread/sleep 600)
                   (println @restart_cnt (reduce (fn [already fork] (+ already @fork)) 0 forks))
                   (recur)
                   )))