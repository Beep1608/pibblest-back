package com.nss.pibblest.modules.analytics.internal.web;

import com.nss.pibblest.modules.analytics.internal.core.AnalyticsService;
import com.nss.pibblest.modules.analytics.internal.web.dto.AnalyticsFilterRequest;
import com.nss.pibblest.modules.analytics.internal.web.dto.CurrencySeriesResponse;
import com.nss.pibblest.modules.analytics.internal.web.dto.RankedDataResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('OWNER')")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/sales-volume")
    public CurrencySeriesResponse getSalesVolume(AnalyticsFilterRequest filter) {
        return analyticsService.getSalesVolume(filter);
    }

    @GetMapping("/sales-count")
    public CurrencySeriesResponse getSalesCount(AnalyticsFilterRequest filter) {
        return analyticsService.getSalesCount(filter);
    }

    @GetMapping("/best-selling-products")
    public RankedDataResponse<?> getBestSellingProducts(AnalyticsFilterRequest filter) {
        return analyticsService.getBestSellingProducts(filter);
    }

    @GetMapping("/peak-hours")
    public RankedDataResponse<?> getPeakHours(AnalyticsFilterRequest filter) {
        return analyticsService.getPeakHours(filter);
    }

    @GetMapping("/average-ticket-value")
    public CurrencySeriesResponse getAverageTicketValue(AnalyticsFilterRequest filter) {
        return analyticsService.getAverageTicketValue(filter);
    }

    @GetMapping("/top-stores")
    public RankedDataResponse<?> getTopStores(AnalyticsFilterRequest filter) {
        return analyticsService.getTopStores(filter);
    }
}
