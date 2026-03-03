package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddPaymentMethodRequest {

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "provider is required")
    private String provider;

    private String last4;
    private String brand;
    private boolean isDefault;
    private String tokenizedRef;
    private String metadata;

    public AddPaymentMethodRequest() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public String getTokenizedRef() { return tokenizedRef; }
    public void setTokenizedRef(String tokenizedRef) { this.tokenizedRef = tokenizedRef; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
