package com.twsela.service;

import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that resolves the correct ECommerceIntegration implementation for a platform.
 */
@Component
public class ECommerceIntegrationFactory {

    private final Map<ECommercePlatform, ECommerceIntegration> integrations;

    public ECommerceIntegrationFactory(List<ECommerceIntegration> integrationList) {
        this.integrations = integrationList.stream()
                .collect(Collectors.toMap(ECommerceIntegration::getPlatform, Function.identity()));
    }

    public ECommerceIntegration getIntegration(ECommercePlatform platform) {
        ECommerceIntegration integration = integrations.get(platform);
        if (integration == null) {
            throw new IllegalArgumentException("منصة غير مدعومة: " + platform);
        }
        return integration;
    }
}
