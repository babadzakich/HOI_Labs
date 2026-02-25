package domain

type Airport struct {
	Code string `json:"code"`
	Name string `json:"name"`
	City City   `json:"city"`
}