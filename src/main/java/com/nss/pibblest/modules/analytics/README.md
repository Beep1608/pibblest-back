# Analytics Module

The analytics module provides read-only aggregation services for owner-level data across stores, sales, and products.

## Key Services

- `AnalyticsService`: Provides core native SQL queries for metrics.
- `AnalyticsController`: Exposes owner-only endpoints.

## Endpoints

- `GET /api/analytics/revenue`
- `GET /api/analytics/sales-count`
- `GET /api/analytics/best-selling-products`
- `GET /api/analytics/peak-hours`
- `GET /api/analytics/store-performance`
- `GET /api/analytics/product-performance`

## Data Model

The module uses Postgres `AT TIME ZONE` for accurate time-series bucketing based on the store's configured timezone.
