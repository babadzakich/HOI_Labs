package getinfo

import (
	"D6/internal/application"
	"context"

	"github.com/jackc/pgx/v5/pgxpool"
)

type PGProvider struct {
	pool *pgxpool.Pool
}

func NewPGProvider(pool *pgxpool.Pool) *PGProvider {
	return &PGProvider{pool: pool}
}

func (p *PGProvider) GetAirportsList(ctx context.Context) ([]application.DataProvider, error) {
	query := "SELECT airport_code, airport_name, city FROM airports"

}
