package domain

type flight struct {
	FlightNo      string `json:"flightNo"`
	DepartureTime string `json:"departure"`
	ArrivalTime   string `json:"arrival"`
}

type Route struct {
	Flights       []flight `json:"flights"`
	Connections   int      `json:"connections"`
	TotalDuration string   `json:"totalDuration"`
}
