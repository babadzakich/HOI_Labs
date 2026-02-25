package domain

type flightBase struct {
	DaysOfWeek	[]int  `json:"daysOfWeek"`
	FlightNo	string `json:"flightNo"`
}

type InboundFlight struct {
	flightBase
	ArrivalTime string `json:"arrivalTime"`
	Origin      string `json:"origin"`
}

type OutboundFlight struct {
	flightBase
	DepartureTime string `json:"departureTime"`
	Destination   string `json:"destination"`
}
