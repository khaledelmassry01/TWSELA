package com.twsela.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for warehouse-courier reconciliation (End of Day).
 */
public class ReconcileRequest {

    @NotEmpty(message = "يجب تحديد شحنة واحدة على الأقل")
    private List<Long> cash_confirmed_shipment_ids;

    private List<Long> returned_shipment_ids;

    public List<Long> getCash_confirmed_shipment_ids() {
        return cash_confirmed_shipment_ids;
    }

    public void setCash_confirmed_shipment_ids(List<Long> cash_confirmed_shipment_ids) {
        this.cash_confirmed_shipment_ids = cash_confirmed_shipment_ids;
    }

    public List<Long> getReturned_shipment_ids() {
        return returned_shipment_ids;
    }

    public void setReturned_shipment_ids(List<Long> returned_shipment_ids) {
        this.returned_shipment_ids = returned_shipment_ids;
    }
}
