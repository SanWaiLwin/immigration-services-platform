package com.swl.hr.system.entity;

import java.io.Serial;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_data", indexes = {
    @Index(name = "idx_job_title", columnList = "job_title"),
    @Index(name = "idx_gender", columnList = "gender"),
    @Index(name = "idx_salary", columnList = "salary")
})
@Data
@NoArgsConstructor
public class JobData extends BaseEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 7667117462132210699L;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "employer")
    private String employer;

    @Column(name = "location")
    private String location;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "years_at_employer", precision = 10, scale = 2)
    private BigDecimal yearsAtEmployer;

    @Column(name = "years_of_experience", precision = 10, scale = 2)
    private BigDecimal yearsOfExperience;

    @Column(name = "salary", precision = 20, scale = 2)
    private BigDecimal salary;

    @Column(name = "signing_bonus", precision = 15, scale = 2)
    private BigDecimal signingBonus;

    @Column(name = "annual_bonus", precision = 15, scale = 2)
    private BigDecimal annualBonus;

    @Column(name = "annual_stock_value_bonus")
    private String annualStockValueBonus;

    @Column(name = "gender")
    private String gender;

    @Column(name = "additional_comments", columnDefinition = "TEXT")
    private String additionalComments;

}