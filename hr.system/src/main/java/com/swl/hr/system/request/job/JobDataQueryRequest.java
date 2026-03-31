package com.swl.hr.system.request.job;

import java.math.BigDecimal;

import com.swl.hr.system.request.common.CommonListQueryRequest;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JobDataQueryRequest extends CommonListQueryRequest {
    // Relaxed: optional filters for read queries
    @Size(max = 100, message = "jobTitle length must be <= 100 characters")
    private String jobTitle;

    @Size(max = 100, message = "gender length must be <= 100 characters")
    private String gender;

    @DecimalMin(value = "0", message = "fromSalary must be >= 0")
    private BigDecimal fromSalary;

    @DecimalMin(value = "0", message = "toSalary must be >= 0")
    private BigDecimal toSalary;

    @AssertTrue(message = "fromSalary must be <= toSalary")
    public boolean isSalaryRangeValid() {
        if (fromSalary == null || toSalary == null) {
            return true; // only validate when both values are provided
        }
        return fromSalary.compareTo(toSalary) <= 0;
    }
}