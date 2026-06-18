package com.nss.pibblest.modules.analytics.internal.core;

import com.nss.pibblest.modules.analytics.internal.web.dto.AnalyticsFilterRequest;
import com.nss.pibblest.modules.analytics.internal.web.dto.BucketEntry;
import com.nss.pibblest.modules.analytics.internal.web.dto.CurrencySeriesResponse;
import com.nss.pibblest.modules.analytics.internal.web.dto.PeakHourEntry;
import com.nss.pibblest.modules.analytics.internal.web.dto.ProductRankEntry;
import com.nss.pibblest.modules.analytics.internal.web.dto.RankedDataResponse;
import com.nss.pibblest.modules.analytics.internal.web.dto.StoreRankEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AnalyticsService {
    private static final Set<String> ALLOWED_GRANULARITIES = Set.of("day", "week", "month");
    private static final int RANK_LIMIT = 10;

    private final JdbcTemplate jdbcTemplate;
    private final DateRangeResolver dateRangeResolver;

    public AnalyticsService(JdbcTemplate jdbcTemplate, DateRangeResolver dateRangeResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.dateRangeResolver = dateRangeResolver;
    }

    public CurrencySeriesResponse getSalesVolume(AnalyticsFilterRequest filter) {
        return currencySeries(filter, "SUM(s.total_amount)");
    }

    public CurrencySeriesResponse getSalesCount(AnalyticsFilterRequest filter) {
        return currencySeries(filter, "COUNT(*)");
    }

    public CurrencySeriesResponse getAverageTicketValue(AnalyticsFilterRequest filter) {
        return currencySeries(filter, "AVG(s.total_amount)");
    }

    public RankedDataResponse<ProductRankEntry> getBestSellingProducts(AnalyticsFilterRequest filter) {
        DateRangeResolver.RangePair range =
            dateRangeResolver.resolve(filter.range(), filter.startDate(), filter.endDate());

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(range.start()));
        params.add(Timestamp.valueOf(range.end()));
        String storeClause = storeClause(filter, params);

        String sql = """
            SELECT p.id AS product_id,
                   p.name AS product_name,
                   SUM(sd.quantity) AS total_quantity
            FROM sales_details sd
            JOIN sales s ON s.id = sd.sale_id
            JOIN products p ON p.id = sd.product_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?
              AND s.created_at <= ?%s
            GROUP BY p.id, p.name
            ORDER BY total_quantity DESC
            LIMIT %d
            """.formatted(storeClause, RANK_LIMIT);

        List<ProductRankEntry> data = jdbcTemplate.query(sql, (rs, rowNum) ->
            new ProductRankEntry(
                rs.getLong("product_id"),
                rs.getString("product_name"),
                rs.getLong("total_quantity")),
            params.toArray());

        return new RankedDataResponse<>(buildFilters(filter), data);
    }

    public RankedDataResponse<PeakHourEntry> getPeakHours(AnalyticsFilterRequest filter) {
        DateRangeResolver.RangePair range =
            dateRangeResolver.resolve(filter.range(), filter.startDate(), filter.endDate());

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(range.start()));
        params.add(Timestamp.valueOf(range.end()));
        String storeClause = storeClause(filter, params);

        String sql = """
            SELECT EXTRACT(HOUR FROM s.created_at AT TIME ZONE st.timezone)::int AS hour,
                   EXTRACT(DOW FROM s.created_at AT TIME ZONE st.timezone)::int AS day_of_week,
                   COUNT(*) AS sale_count
            FROM sales s
            JOIN stores st ON st.id = s.store_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?
              AND s.created_at <= ?%s
            GROUP BY hour, day_of_week
            ORDER BY hour, day_of_week
            """.formatted(storeClause);

        List<PeakHourEntry> data = jdbcTemplate.query(sql, (rs, rowNum) ->
            new PeakHourEntry(
                rs.getInt("hour"),
                rs.getInt("day_of_week"),
                rs.getLong("sale_count")),
            params.toArray());

        return new RankedDataResponse<>(buildFilters(filter), data);
    }

    public RankedDataResponse<StoreRankEntry> getTopStores(AnalyticsFilterRequest filter) {
        DateRangeResolver.RangePair range =
            dateRangeResolver.resolve(filter.range(), filter.startDate(), filter.endDate());

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(range.start()));
        params.add(Timestamp.valueOf(range.end()));
        String storeClause = storeClause(filter, params);

        String sql = """
            SELECT st.id AS store_id,
                   st.name AS store_name,
                   s.currency_code AS currency_code,
                   SUM(s.total_amount) AS total_revenue
            FROM sales s
            JOIN stores st ON st.id = s.store_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?
              AND s.created_at <= ?%s
            GROUP BY st.id, st.name, s.currency_code
            ORDER BY total_revenue DESC
            LIMIT %d
            """.formatted(storeClause, RANK_LIMIT);

        List<StoreRankEntry> data = jdbcTemplate.query(sql, (rs, rowNum) ->
            new StoreRankEntry(
                rs.getLong("store_id"),
                rs.getString("store_name"),
                rs.getBigDecimal("total_revenue"),
                rs.getString("currency_code")),
            params.toArray());

        return new RankedDataResponse<>(buildFilters(filter), data);
    }

    /**
     * Builds a per-currency time series for a numeric aggregate of completed sales,
     * bucketed by the requested granularity in each store's configured timezone.
     */
    private CurrencySeriesResponse currencySeries(AnalyticsFilterRequest filter, String aggregate) {
        String granularity = resolveGranularity(filter.granularity());
        DateRangeResolver.RangePair range =
            dateRangeResolver.resolve(filter.range(), filter.startDate(), filter.endDate());

        List<Object> params = new ArrayList<>();
        params.add(granularity);
        params.add(Timestamp.valueOf(range.start()));
        params.add(Timestamp.valueOf(range.end()));
        String storeClause = storeClause(filter, params);

        String sql = """
            SELECT s.currency_code AS currency_code,
                   to_char(date_trunc(?, s.created_at AT TIME ZONE st.timezone), 'YYYY-MM-DD') AS bucket,
                   %s AS value
            FROM sales s
            JOIN stores st ON st.id = s.store_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?
              AND s.created_at <= ?%s
            GROUP BY s.currency_code, bucket
            ORDER BY bucket
            """.formatted(aggregate, storeClause);

        Map<String, List<BucketEntry>> series = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String currency = rs.getString("currency_code");
            BucketEntry entry = new BucketEntry(
                rs.getString("bucket"),
                Optional.ofNullable(rs.getBigDecimal("value")).orElse(BigDecimal.ZERO));
            series.computeIfAbsent(currency, key -> new ArrayList<>()).add(entry);
        }, params.toArray());

        return new CurrencySeriesResponse(buildFilters(filter), series);
    }

    /**
     * Appends an optional store filter to the WHERE clause and registers its bind param.
     * Returns the SQL fragment to splice in (empty when no store filter is requested).
     */
    private String storeClause(AnalyticsFilterRequest filter, List<Object> params) {
        if (filter.storeId() == null) {
            return "";
        }
        params.add(filter.storeId());
        return " AND s.store_id = ?";
    }

    private String resolveGranularity(String granularity) {
        if (granularity == null || !ALLOWED_GRANULARITIES.contains(granularity)) {
            throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }
        return granularity;
    }

    private Map<String, Object> buildFilters(AnalyticsFilterRequest filter) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("range", filter.range());
        map.put("granularity", filter.granularity());
        if (filter.startDate() != null) {
            map.put("startDate", filter.startDate().toString());
        }
        if (filter.endDate() != null) {
            map.put("endDate", filter.endDate().toString());
        }
        if (filter.storeId() != null) {
            map.put("storeId", filter.storeId());
        }
        return map;
    }
}
