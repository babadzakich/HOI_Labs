SELECT DISTINCT
    r.route_no,
    s.fare_conditions,
    r.duration,
    (pr.default_price * EXTRACT(EPOCH FROM r.duration) / 60)::numeric(10,2) as calculated_price,
    s.price as actual_price
FROM routes r
JOIN flights f ON r.route_no = f.route_no 
              AND r.validity @> f.scheduled_departure
JOIN segments s ON f.flight_id = s.flight_id
JOIN pricing_rule pr ON s.fare_conditions = pr.fare_conditions
WHERE f.status = 'Arrived'
ORDER BY r.route_no, s.fare_conditions, r.duration;
