package com.lms.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId; 

    @Column(nullable = false)
    private Long loanTypeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private Integer tenure; // in months

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal annualIncome;

    @Column(precision = 5, scale = 2)
    private BigDecimal employmentScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.APPLIED;

    @Column(precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal approvedInterestRate;

    @Column(columnDefinition = "TEXT")
    private String approvalRemarks;

    @Column
    private Long approvedBy; 
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedDate;

    @Column
    private LocalDateTime approvalDate;

    @Column
    private LocalDateTime closedDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
