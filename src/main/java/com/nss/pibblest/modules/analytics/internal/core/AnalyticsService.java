package com.nss.pibblest.modules.analytics.internal.core;

import com.nss.pibblest.modules.analytics.internal.web.dto.AnalyticsFilterRequest;
import com.nss.pibblest.modules.analytics.internal.web.dto.BucketEntry;
import com.nss.pibblest.modules.analytics.internal.web.dto.CurrencySeriesResponse;
import com.nss.pibblest.modules.analytics.internal.web.dto.PeakHourEntry;
import com.nss.pibblest.modules.analytics.internal.web.dto.ProductRankEntry;
import com.nss.pibblest.modules.analytics.internal.web.dto.RankedDataResponse;
import com.nss.pibblest.modules.analytics.internal.web.dto.StoreRankEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only analytics aggregations over completed sales.
 *
 * <p>Queries run through the JPA {@link EntityManager} on purpose: Hibernate routes
 * its connections through the multi-tenant {@code TenantConnectionProvider}, which sets
 * the Postgres {@code search_path} to the current tenant schema. A raw {@code JdbcTemplate}
 * would bypass that routing and hit the default {@code identity} schema, where these
 * tables do not exist.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {
    private static final Set<String> ALLOWED_GRANULARITIES = Set.of("day", "week", "month");
    private static final int RANK_LIMIT = 10;

    @PersistenceContext
    private EntityManager entityManager;

    private final DateRangeResolver dateRangeResolver;

    public AnalyticsService(DateRangeResolver dateRangeResolver) {
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
        boolean byStore = filter.storeId() != null;

        String sql = """
            SELECT p.id AS product_id,
                   p.name AS product_name,
                   SUM(sd.quantity) AS total_quantity
            FROM sales_details sd
            JOIN sales s ON s.id = sd.sale_id
            JOIN products p ON p.id = sd.product_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?1
              AND s.created_at <= ?2%s
            GROUP BY p.id, p.name
            ORDER BY total_quantity DESC
            LIMIT %d
            """.formatted(byStore ? " AND s.store_id = ?3" : "", RANK_LIMIT);

        Query query = entityManager.createNativeQuery(sql);
        bindRange(query, range, byStore, filter.storeId());

        List<ProductRankEntry> data = new ArrayList<>();
        for (Object[] row : rows(query)) {
            data.add(new ProductRankEntry(toLong(row[0]), (String) row[1], toLong(row[2])));
        }
        return new RankedDataResponse<>(buildFilters(filter), data);
    }

    public RankedDataResponse<PeakHourEntry> getPeakHours(AnalyticsFilterRequest filter) {
        DateRangeResolver.RangePair range =
            dateRangeResolver.resolve(filter.range(), filter.startDate(), filter.endDate());
        boolean byStore = filter.storeId() != null;

        String sql = """
            SELECT EXTRACT(HOUR FROM s.created_at AT TIME ZONE st.timezone)::int AS hour,
                   EXTRACT(DOW FROM s.created_at AT TIME ZONE st.timezone)::int AS day_of_week,
                   COUNT(*) AS sale_count
            FROM sales s
            JOIN stores st ON st.id = s.store_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?1
              AND s.created_at <= ?2%s
            GROUP BY hour, day_of_week
            ORDER BY hour, day_of_week
            """.formatted(byStore ? " AND s.store_id = ?3" : "");

        Query query = entityManager.createNativeQuery(sql);
        bindRange(query, range, byStore, filter.storeId());

        List<PeakHourEntry> data = new ArrayList<>();
        for (Object[] row : rows(query)) {
            data.add(new PeakHourEntry(toInt(row[0]), toInt(row[1]), toLong(row[2])));
        }
        return new RankedDataResponse<>(buildFilters(filter), data);
    }

    public RankedDataResponse<StoreRankEntry> getTopStores(AnalyticsFilterRequest filter) {
        DateRangeResolver.RangePair range =
            dateRangeResolver.resolve(filter.range(), filter.startDate(), filter.endDate());
        boolean byStore = filter.storeId() != null;

        String sql = """
            SELECT st.id AS store_id,
                   st.name AS store_name,
                   SUM(s.total_amount) AS total_revenue,
                   s.currency_code AS currency_code
            FROM sales s
            JOIN stores st ON st.id = s.store_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?1
              AND s.created_at <= ?2%s
            GROUP BY st.id, st.name, s.currency_code
            ORDER BY total_revenue DESC
            LIMIT %d
            """.formatted(byStore ? " AND s.store_id = ?3" : "", RANK_LIMIT);

        Query query = entityManager.createNativeQuery(sql);
        bindRange(query, range, byStore, filter.storeId());

        List<StoreRankEntry> data = new ArrayList<>();
        for (Object[] row : rows(query)) {
            data.add(new StoreRankEntry(toLong(row[0]), (String) row[1], toBigDecimal(row[2]), (String) row[3]));
        }
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
        boolean byStore = filter.storeId() != null;

        // granularity is whitelisted and aggregate is an internal constant: safe to inline.
        String sql = """
            SELECT s.currency_code AS currency_code,
                   to_char(date_trunc('%s', s.created_at AT TIME ZONE st.timezone), 'YYYY-MM-DD') AS bucket,
                   %s AS value
            FROM sales s
            JOIN stores st ON st.id = s.store_id
            WHERE s.deleted_at IS NULL
              AND s.status = 'COMPLETED'
              AND s.created_at >= ?1
              AND s.created_at <= ?2%s
            GROUP BY s.currency_code, bucket
            ORDER BY bucket
            """.formatted(granularity, aggregate, byStore ? " AND s.store_id = ?3" : "");

        Query query = entityManager.createNativeQuery(sql);
        bindRange(query, range, byStore, filter.storeId());

        Map<String, List<BucketEntry>> series = new LinkedHashMap<>();
        for (Object[] row : rows(query)) {
            String currency = (String) row[0];
            BucketEntry entry = new BucketEntry((String) row[1], toBigDecimal(row[2]));
            series.computeIfAbsent(currency, key -> new ArrayList<>()).add(entry);
        }
        return new CurrencySeriesResponse(buildFilters(filter), series);
    }

    private void bindRange(Query query, DateRangeResolver.RangePair range, boolean byStore, Long storeId) {
        query.setParameter(1, Timestamp.valueOf(range.start()));
        query.setParameter(2, Timestamp.valueOf(range.end()));
        if (byStore) {
            query.setParameter(3, storeId);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> rows(Query query) {
        return query.getResultList();
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

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }
}
