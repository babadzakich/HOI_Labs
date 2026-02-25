package application

type DataProvider interface {
	GetAirportsList() []string
}
