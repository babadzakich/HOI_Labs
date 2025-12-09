(ns c6.core)

;;;an empty route map
;;;it is enough to use either forward or backward part (they correspond to each other including shared reference to number of tickets)
;;;:forward is a map with route start point names as keys and nested map as values
;;;each nested map has route end point names as keys and route descriptor as values
;;;each route descriptor is a map (structure in fact) of the fixed structure where
;;;:price contains ticket price
;;;and :tickets contains reference to tickets number
;;;:backward has the same structure but start and end points are reverted
(def empty-map
  {:forward {},
   :backward {}})

(def transaction-count (atom 0))

(defn route
  "Add a new route (route) to the given route map
   route-map - route map to modify
   from - name (string) of the start point of the route
   to - name (string) of the end poiunt of the route
   price - ticket price
   tickets-num - number of tickets available"
  [route-map from to price tickets-num]
  (let [tickets (ref tickets-num :validator (fn [state] (>= state 0))),     ;reference for the number of tickets
        orig-source-desc (or (get-in route-map [:forward from]) {}),
        orig-reverse-dest-desc (or (get-in route-map [:backward to]) {}),
        route-desc {:price price,                                            ;route descriptor
                    :tickets tickets},
        source-desc (assoc orig-source-desc to route-desc),
        reverse-dest-desc (assoc orig-reverse-dest-desc from route-desc)]
    (-> route-map
        (assoc-in [:forward from] source-desc)
        (assoc-in [:backward to] reverse-dest-desc))))

(defn book-tickets
  "Tries to book tickets and decrement appropriate references in route-map atomically
   returns map with either :price (for the whole route) and :path (a list of destination names) keys
          or with :error key that indicates that booking is impossible due to lack of tickets"
  [route-map from to]
  (if (= from to)
    {:path '(), :price 0}
    (try
      (dosync
        (swap! transaction-count (partial + 1))
        (let [
              other-nodes (filter #(not= from %) (keys (route-map :backward)))
              finalDijkstraState (nth (iterate (fn [currentState]
                                                 (let [
                                                       nextId (apply min-key (fn [key] (get-in currentState [:not-visited key :distance])) (keys (currentState :not-visited))),
                                                       next (get-in currentState [:not-visited nextId]),
                                                       relaxedState (reduce-kv
                                                                      (fn [state to route-desc]
                                                                        (let [relaxed-dist (+ (route-desc :price) (next :distance)), node (get-in state [:not-visited to])]
                                                                          (if (and (not= node nil) (< relaxed-dist (node :distance)))
                                                                            (assoc state :not-visited (assoc (state :not-visited) to {:node to, :distance relaxed-dist, :prev nextId}))
                                                                            state
                                                                            )
                                                                          )
                                                                        )
                                                                      currentState
                                                                      (or (select-keys (get-in route-map [:forward nextId]) (filter #(> @(get-in route-map [:forward nextId % :tickets]) 0) (keys ((route-map :forward) nextId)))) {})
                                                                      ),
                                                       ]
                                                   {:visited (assoc (relaxedState :visited) nextId next), :not-visited (dissoc (relaxedState :not-visited) nextId)}
                                                   )
                                                 )
                                               {:visited {}, :not-visited (assoc (zipmap other-nodes (map (fn [id] {:node id, :distance 1000000, :prev id}) other-nodes)) from {:node from, :distance 0, :prev from})}
                                               )
                                      (count (keys (route-map :backward)))
                                      ),
              path (->>
                     (iterate #(get-in finalDijkstraState [:visited % :prev]) to)
                     (take (+ (count other-nodes) 1))
                     (take-while #(not= % from))
                     reverse
                     (cons from)),
              path-len (count path),
              nexts (concat (drop 1 path) (list (first path))),
              foreach-flight #(map % (take (- path-len 1) path) (take (- path-len 1) nexts))
              ]
          (->
            (fn [prev next] (commute (get-in route-map [:forward prev next :tickets]) #(- % 1)))
            foreach-flight
            doall
            )
          {:path path, :price (reduce + (foreach-flight (fn [p n] (get-in route-map [:forward p n :price]))))}
          )
        )
      (catch Exception e { :error "Failed to book" })
      )
    )
  )


;;;cities
(def spec1 (-> empty-map
               (route "City1" "Capital"    200 5)
               (route "Capital" "City1"    250 5)
               (route "City2" "Capital"    200 5)
               (route "Capital" "City2"    250 5)
               (route "City3" "Capital"    300 3)
               (route "Capital" "City3"    400 3)
               (route "City1" "Town1_X"    50 2)
               (route "Town1_X" "City1"    150 2)
               (route "Town1_X" "TownX_2"  50 2)
               (route "TownX_2" "Town1_X"  150 2)
               (route "Town1_X" "TownX_2"  50 2)
               (route "TownX_2" "City2"    50 3)
               (route "City2" "TownX_2"    150 3)
               (route "City2" "Town2_3"    50 2)
               (route "Town2_3" "City2"    150 2)
               (route "Town2_3" "City3"    50 3)
               (route "City3" "Town2_3"    150 2)))

(defn booking-future [route-map from to init-delay loop-delay]
  (future
    (Thread/sleep init-delay)
    (loop [bookings []]
      (Thread/sleep loop-delay)
      (let [booking (book-tickets route-map from to)]
        (if (booking :error)
          bookings
          ;(conj bookings booking)
          (recur (conj bookings booking))
          )))))

(defn print-bookings [name ft]
  (println (str name ":") (count ft) "bookings")
  (doseq [booking ft]
    (println "price:" (booking :price) "path:" (booking :path) )))

(defn run []
  ;;try to tune timeouts in order to all the customers gain at least one booking
  (let [
        f1 (booking-future spec1 "City1" "City3" 100 100),
        f2 (booking-future spec1 "City1" "City2" 100 100),
        f3 (booking-future spec1 "City2" "City3" 100 100)
        ]
    (print-bookings "City1->City3:" @f1)
    (print-bookings "City1->City2:" @f2)
    (print-bookings "City2->City3:" @f3)
    ;;replace with you mechanism to monitor a number of transaction restarts
    (println "Total (re-)starts:" @transaction-count)
    ))
(run)