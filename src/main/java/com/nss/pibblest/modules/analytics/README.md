# Analytics Module

The analytics module provides read-only aggregation services for owner-level data across stores, sales, and products.

## Key Services

- `AnalyticsService`: Provides core native SQL queries for metrics.
- `AnalyticsController`: Exposes owner-only endpoints.

## Endpoints

- `GET /api/analytics/sales-volume`
- `GET /api/analytics/sales-count`
- `GET /api/analytics/average-ticket-value`
- `GET /api/analytics/best-selling-products`
- `GET /api/analytics/peak-hours`
- `GET /api/analytics/top-stores`

## Data Model

The module uses Postgres `AT TIME ZONE` for accurate time-series bucketing based on the store's configured timezone.
