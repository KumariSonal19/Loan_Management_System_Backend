package com.lms.loan.service;

import com.lms.loan.client.AdminClient; 
import com.lms.loan.client.EmiClient;
import com.lms.loan.client.NotificationClient;
import com.lms.loan.client.UserClient;
import com.lms.loan.dto.LoanApplicationDTO;
import com.lms.loan.dto.LoanApprovalRequestDTO;
import com.lms.loan.dto.LoanTypeDTO; 
import com.lms.loan.entity.LoanApplication;
import com.lms.loan.entity.LoanStatus;
import com.lms.loan.repository.LoanApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class LoanApplicationService {

    @Autowired
    private EmiClient emiClient;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private AdminClient adminClient; 

    @Autowired(required = false)
    private NotificationClient notificationClient;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserClient userClient;

    public LoanApplicationDTO applyLoan(Long customerId, LoanApplicationDTO dto) {
        
        LoanTypeDTO loanType = adminClient.getLoanTypeById(dto.getLoanTypeId());

        if (loanType == null) {
             throw new RuntimeException("Loan type not found");
        }

        if (dto.getLoanAmount().compareTo(loanType.getMinAmount()) < 0) {
            throw new RuntimeException("Loan amount is below minimum: " + loanType.getMinAmount());
        }
        if (dto.getLoanAmount().compareTo(loanType.getMaxAmount()) > 0) {
            throw new RuntimeException("Loan amount exceeds maximum: " + loanType.getMaxAmount());
        }
        if (dto.getTenure() < loanType.getMinTenure() || dto.getTenure() > loanType.getMaxTenure()) {
            throw new RuntimeException("Tenure must be between " + loanType.getMinTenure() +
                    " and " + loanType.getMaxTenure() + " months");
        }

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomerId(customerId);
        loanApplication.setLoanTypeId(loanType.getId()); 
        loanApplication.setLoanAmount(dto.getLoanAmount());
        loanApplication.setTenure(dto.getTenure());
        loanApplication.setAnnualIncome(dto.getAnnualIncome());
        loanApplication.setEmploymentScore(dto.getEmploymentScore());
        loanApplication.setStatus(LoanStatus.APPLIED);
        loanApplication.setAppliedDate(LocalDateTime.now());
        
        LoanApplication savedLoan = loanApplicationRepository.save(loanApplication);
        log.info("Loan application created - ID: {}, Customer: {}", savedLoan.getId(), customerId);
        
        String userEmail = getUserEmail(customerId);
        notificationService.sendNotification(
            customerId,
            savedLoan.getId(),
            userEmail, 
            "LOAN_APPLIED", 
            "Loan Received",
            "Your loan application has been received."
        );

        return mapToDTO(savedLoan);
    }

    public LoanApplicationDTO updateLoanStatus(LoanApprovalRequestDTO request, Long officerId) {
        LoanApplication loan = loanApplicationRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan application not found"));

        LoanStatus newStatus;
        try {
            newStatus = LoanStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Invalid status. Allowed: UNDER_REVIEW, APPROVED, REJECTED");
        }

        log.info("Processing status update for Loan ID: {} to {}", loan.getId(), newStatus);

        String userEmail = getUserEmail(loan.getCustomerId());

        switch (newStatus) {
            case UNDER_REVIEW:
                if (loan.getStatus() != LoanStatus.APPLIED) {
                    throw new RuntimeException("Only APPLIED loans can be moved to UNDER_REVIEW");
                }
                loan.setStatus(LoanStatus.UNDER_REVIEW);
                loan.setApprovalRemarks(request.getRemarks());
                break;

            case APPROVED:
                if (loan.getStatus() != LoanStatus.APPLIED && loan.getStatus() != LoanStatus.UNDER_REVIEW) {
                    throw new RuntimeException("Loan cannot be approved from current status: " + loan.getStatus());
                }
                if (request.getApprovedAmount() == null || request.getInterestRate() == null) {
                    throw new RuntimeException("Approved Amount and Interest Rate are required for Approval");
                }

                loan.setStatus(LoanStatus.APPROVED);
                loan.setApprovedAmount(request.getApprovedAmount());
                loan.setApprovedInterestRate(request.getInterestRate());
                loan.setApprovalRemarks(request.getRemarks());
                loan.setApprovedBy(officerId);
                loan.setApprovalDate(LocalDateTime.now());

                try {
                    EmiClient.EMIGenerationRequest emiRequest = new EmiClient.EMIGenerationRequest(
                            loan.getId(),
                            request.getApprovedAmount(),
                            request.getInterestRate(),
                            loan.getTenure()
                    );
                    emiClient.generateEMISchedule(emiRequest);
                    log.info("EMI Generation triggered for Loan ID: {}", loan.getId());
                } catch (Exception e) {
                    log.error("CRITICAL: Loan approved but EMI generation failed: {}", e.getMessage());
                }

                String notifType = "LOAN_" + request.getStatus();
                notificationService.sendNotification(
                    loan.getCustomerId(),
                    loan.getId(),
                    userEmail, 
                    notifType,
                    "Loan Status Update",
                    "Your loan is now " + request.getStatus()
                );
                break;

            case REJECTED:
                if (loan.getStatus() == LoanStatus.CLOSED || loan.getStatus() == LoanStatus.APPROVED) {
                    throw new RuntimeException("Cannot reject a closed or approved loan");
                }
                loan.setStatus(LoanStatus.REJECTED);
                loan.setApprovalRemarks(request.getRemarks());
                loan.setApprovedBy(officerId);
                loan.setApprovalDate(LocalDateTime.now());

                notificationService.sendNotification(
                    loan.getCustomerId(),
                    loan.getId(),
                    userEmail, 
                    "LOAN_REJECTED",
                    "Loan Application Rejected",
                    "Your loan application has been rejected. Remarks: " + request.getRemarks()
                );
                break;

            default:
                throw new RuntimeException("Invalid status transition requested");
        }

        return mapToDTO(loanApplicationRepository.save(loan));
    }

    public LoanApplicationDTO getLoanById(Long loanId) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return mapToDTO(loan);
    }

    public List<LoanApplicationDTO> getCustomerLoans(Long customerId) {
        return loanApplicationRepository.findByCustomerId(customerId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<LoanApplicationDTO> getLoansByStatusList(LoanStatus status) {
        return loanApplicationRepository.findByStatus(status)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Long getLoansCountByStatus(LoanStatus status) {
        return loanApplicationRepository.countByStatus(status);
    }

    public void closeLoan(Long loanId) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedDate(LocalDateTime.now());
        loanApplicationRepository.save(loan);

        log.info("Loan closed - ID: {}", loanId);
        sendNotificationViaFeign(loan.getCustomerId(), "LOAN_CLOSED", "Your loan has been completely repaid", loan.getId());
    }

    private void sendNotificationViaFeign(Long customerId, String type, String message, Long loanId) {
        if (notificationClient != null) {
            try {
                notificationClient.sendLoanNotification(customerId, type, message, loanId);
            } catch (Exception e) {
                log.warn("Could not send notification via Feign: {}", e.getMessage());
            }
        }
    }

    private String getUserEmail(Long customerId) {
        try {
            com.lms.loan.dto.UserDTO user = userClient.getUserById(customerId);
            if (user != null && user.getEmail() != null) {
                return user.getEmail();
            }
        } catch (Exception e) {
            log.error("Failed to fetch email for user {}: {}", customerId, e.getMessage());
        }
        return "sonalkumari6688@gmail.com"; 
    }

    private LoanApplicationDTO mapToDTO(LoanApplication loan) {
        LoanApplicationDTO dto = new LoanApplicationDTO();
        dto.setId(loan.getId());
        dto.setLoanTypeId(loan.getLoanTypeId()); 
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setTenure(loan.getTenure());
        dto.setAnnualIncome(loan.getAnnualIncome());
        dto.setEmploymentScore(loan.getEmploymentScore());
        dto.setStatus(loan.getStatus());
        dto.setApprovedAmount(loan.getApprovedAmount());
        dto.setApprovedInterestRate(loan.getApprovedInterestRate());
        dto.setApprovalRemarks(loan.getApprovalRemarks());
        dto.setAppliedDate(loan.getAppliedDate());
        dto.setApprovalDate(loan.getApprovalDate());
        dto.setClosedDate(loan.getClosedDate());
        return dto;
    }

    public List<LoanApplicationDTO> getAllLoans() {
        return loanApplicationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}