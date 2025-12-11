package com.twsela.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final Counter loginAttemptsCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter shipmentCreatedCounter;
    private final Counter shipmentDeliveredCounter;
    private final Timer shipmentProcessingTimer;
    private final AtomicLong activeUsersGauge;
    private final AtomicLong activeShipmentsGauge;

    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.loginAttemptsCounter = Counter.builder("twsela.login.attempts")
                .description("Total number of login attempts")
                .register(meterRegistry);
                
        this.loginSuccessCounter = Counter.builder("twsela.login.success")
                .description("Total number of successful logins")
                .register(meterRegistry);
                
        this.loginFailureCounter = Counter.builder("twsela.login.failures")
                .description("Total number of failed logins")
                .register(meterRegistry);
                
        this.shipmentCreatedCounter = Counter.builder("twsela.shipments.created")
                .description("Total number of shipments created")
                .register(meterRegistry);
                
        this.shipmentDeliveredCounter = Counter.builder("twsela.shipments.delivered")
                .description("Total number of shipments delivered")
                .register(meterRegistry);
                
        this.shipmentProcessingTimer = Timer.builder("twsela.shipments.processing.time")
                .description("Time taken to process shipments")
                .register(meterRegistry);
        
        // Initialize gauges
        this.activeUsersGauge = new AtomicLong(0);
        this.activeShipmentsGauge = new AtomicLong(0);
        
        // Register gauges using a different approach
        meterRegistry.gauge("twsela.users.active", activeUsersGauge, AtomicLong::get);
        meterRegistry.gauge("twsela.shipments.active", activeShipmentsGauge, AtomicLong::get);
    }

    // Authentication metrics
    public void recordLoginAttempt() {
        loginAttemptsCounter.increment();
    }

    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void recordLoginFailure() {
        loginFailureCounter.increment();
    }

    // Shipment metrics
    public void recordShipmentCreated() {
        shipmentCreatedCounter.increment();
    }

    public void recordShipmentDelivered() {
        shipmentDeliveredCounter.increment();
    }

    public Timer.Sample startShipmentProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordShipmentProcessingTime(Timer.Sample sample) {
        sample.stop(shipmentProcessingTimer);
    }

    // Gauge updates
    public void updateActiveUsersCount(long count) {
        activeUsersGauge.set(count);
    }

    public void updateActiveShipmentsCount(long count) {
        activeShipmentsGauge.set(count);
    }

    // Custom metrics for business logic
    public void recordApiCall(String endpoint, String method, int statusCode) {
        Counter.builder("twsela.api.calls")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .description("API call metrics")
                .register(meterRegistry)
                .increment();
    }

    public void recordDatabaseQuery(String queryType, long executionTimeMs) {
        Timer.builder("twsela.database.query.time")
                .tag("query_type", queryType)
                .description("Database query execution time")
                .register(meterRegistry)
                .record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void recordCacheHit(String cacheName) {
        Counter.builder("twsela.cache.hits")
                .tag("cache_name", cacheName)
                .description("Cache hit count")
                .register(meterRegistry)
                .increment();
    }

    public void recordCacheMiss(String cacheName) {
        Counter.builder("twsela.cache.misses")
                .tag("cache_name", cacheName)
                .description("Cache miss count")
                .register(meterRegistry)
                .increment();
    }

    public void recordError(String errorType, String component) {
        Counter.builder("twsela.errors")
                .tag("error_type", errorType)
                .tag("component", component)
                .description("Application errors")
                .register(meterRegistry)
                .increment();
    }
}
