package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.web.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory to resolve the appropriate PaymentGateway implementation by type.
 */
@Component
public class PaymentGatewayFactory {

    private final Map<PaymentGatewayType, PaymentGateway> gateways;

    public PaymentGatewayFactory(List<PaymentGateway> gatewayList) {
        this.gateways = new EnumMap<>(PaymentGatewayType.class);
        for (PaymentGateway gateway : gatewayList) {
            gateways.put(gateway.getGatewayType(), gateway);
        }
    }

    /**
     * Get a payment gateway by type.
     *
     * @param type the gateway type
     * @return the gateway implementation
     * @throws BusinessRuleException if no implementation found
     */
    public PaymentGateway getGateway(PaymentGatewayType type) {
        PaymentGateway gateway = gateways.get(type);
        if (gateway == null) {
            throw new BusinessRuleException("بوابة الدفع غير مدعومة: " + type);
        }
        return gateway;
    }

    /**
     * Check if a gateway type is supported.
     */
    public boolean isSupported(PaymentGatewayType type) {
        return gateways.containsKey(type);
    }
}
