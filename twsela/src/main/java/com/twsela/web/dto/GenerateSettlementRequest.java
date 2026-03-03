package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class GenerateSettlementRequest {

    @NotBlank(message = "period is required")
    private String period;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    public GenerateSettlementRequest() {}

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
