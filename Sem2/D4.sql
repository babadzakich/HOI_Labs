CREATE TABLE pricing_rules (
    route_no text NOT NULL,
    fare_conditions text NOT NULL,
    base_price numeric(10,2) NOT NULL,
    min_price numeric(10,2) NOT NULL,
    max_price numeric(10,2) NOT NULL,
    sample_count integer NOT NULL,
    last_updated timestamp with time zone DEFAULT now(),
    PRIMARY KEY (route_no, fare_conditions)
);

-- Build pricing rules from historical bookings (Arrived flights only)
INSERT INTO pricing_rules (route_no, fare_conditions, base_price, min_price, max_price, sample_count)
SELECT 
    r.route_no,
    s.fare_conditions,
    percentile_cont(0.5) WITHIN GROUP (ORDER BY s.price) as base_price,
    MIN(s.price) as min_price,
    MAX(s.price) as max_price,
    COUNT(*) as sample_count
FROM routes r
JOIN flights f ON r.route_no = f.route_no
JOIN segments s ON f.flight_id = s.flight_id
WHERE f.status = 'Arrived'
GROUP BY r.route_no, s.fare_conditions;
