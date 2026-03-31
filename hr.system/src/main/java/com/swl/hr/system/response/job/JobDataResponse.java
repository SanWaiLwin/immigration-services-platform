package com.swl.hr.system.response.job;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class JobDataResponse {
    private String jobTitle;
    private BigDecimal salary;
    private String gender;
}