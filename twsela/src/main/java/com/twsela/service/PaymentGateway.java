package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface for payment gateway integrations.
 * Each gateway (Paymob, Stripe, Fawry, etc.) implements this interface.
 */
public interface PaymentGateway {

    /**
     * Get the gateway type this implementation handles.
     */
    PaymentGatewayType getGatewayType();

    /**
     * Initiate a charge/payment.
     *
     * @param amount   the amount to charge
     * @param currency the currency code (e.g., "EGP")
     * @param metadata additional data (invoiceId, merchantId, etc.)
     * @return external transaction ID from the gateway
     */
    String charge(BigDecimal amount, String currency, Map<String, String> metadata);

    /**
     * Refund a previous transaction.
     *
     * @param externalTransactionId the gateway's transaction ID
     * @param amount                amount to refund
     * @return external refund ID
     */
    String refund(String externalTransactionId, BigDecimal amount);

    /**
     * Verify a webhook signature/payload from the gateway.
     *
     * @param payload   the raw webhook body
     * @param signature the signature header value
     * @return true if valid
     */
    boolean verifyWebhook(String payload, String signature);
}
