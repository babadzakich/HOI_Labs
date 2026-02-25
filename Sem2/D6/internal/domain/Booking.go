package domain

import (
	"encoding/json"
	"fmt"
)

type BookingClass int

const (
	Economy BookingClass = iota // 0
	Comfort                     // 1
	Business                    // 2
)

var (
	toName = map[BookingClass]string{
		Economy:  "Economy",
		Comfort:  "Comfort",
		Business: "Business",
	}
	toID = map[string]BookingClass{
		"Economy":  Economy,
		"Comfort":  Comfort,
		"Business": Business,
	}
)

// MarshalJSON превращает BookingClass в строку для JSON
func (b BookingClass) MarshalJSON() ([]byte, error) {
	name, ok := toName[b]
	if !ok {
		return nil, fmt.Errorf("unknown booking class ID: %d", b)
	}
	return json.Marshal(name)
}

// UnmarshalJSON превращает строку из JSON обратно в BookingClass
func (b *BookingClass) UnmarshalJSON(data []byte) error {
	var s string
	if err := json.Unmarshal(data, &s); err != nil {
		return err
	}

	id, ok := toID[s]
	if !ok {
		return fmt.Errorf("invalid booking class name: %s", s)
	}

	*b = id
	return nil
}

// String() полезен для логирования и отладки
func (b BookingClass) String() string {
	return toName[b]
}

type Passenger struct {
	FirstName  string `json:"firstName"`
	LastName   string `json:"lastName"`
	PassportNo string `json:"passportNo"`
}

type Booking struct {
	FlightId  string       `json:"flight_id"`
	Class     BookingClass `json:"booking_class"`
	Passenger Passenger    `json:"passenger"`
}
