package com.lms.loan.service;

import com.lms.loan.client.AdminClient;
import com.lms.loan.client.EmiClient;
import com.lms.loan.client.NotificationClient;
import com.lms.loan.client.UserClient;
import com.lms.loan.dto.LoanApplicationDTO;
import com.lms.loan.dto.LoanApprovalRequestDTO;
import com.lms.loan.dto.LoanTypeDTO;
import com.lms.loan.dto.UserDTO;
import com.lms.loan.entity.LoanApplication;
import com.lms.loan.entity.LoanStatus;
import com.lms.loan.repository.LoanApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @InjectMocks
    private LoanApplicationService service;

    @Mock
    private LoanApplicationRepository loanRepo;

    @Mock
    private AdminClient adminClient;

    @Mock
    private EmiClient emiClient;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private UserClient userClient;

    @Test
    void applyLoan_success() {
        
        LoanTypeDTO typeDTO = new LoanTypeDTO();
        typeDTO.setId(1L);
        typeDTO.setMinAmount(BigDecimal.valueOf(1000));
        typeDTO.setMaxAmount(BigDecimal.valueOf(500000));
        typeDTO.setMinTenure(6);
        typeDTO.setMaxTenure(60);

        when(adminClient.getLoanTypeById(1L)).thenReturn(typeDTO); 
        
        when(loanRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        
        when(userClient.getUserById(anyLong()))
                .thenReturn(new UserDTO(1L, "a@b.com", "User"));

        LoanApplicationDTO dto = new LoanApplicationDTO();
        dto.setLoanTypeId(1L);
        dto.setLoanAmount(BigDecimal.valueOf(10000));
        dto.setTenure(12);
        dto.setAnnualIncome(BigDecimal.valueOf(500000));

        LoanApplicationDTO result = service.applyLoan(1L, dto);

        assertEquals(LoanStatus.APPLIED, result.getStatus());
        assertEquals(1L, result.getLoanTypeId()); 
        verify(notificationService).sendNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateLoanStatus_approve_success() {
        // Setup Loan
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setLoanTypeId(1L); 
        loan.setStatus(LoanStatus.APPLIED);
        loan.setTenure(12);
        loan.setCustomerId(5L);

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDTO user = new UserDTO();
        user.setId(5L);
        user.setEmail("x@y.com");
        when(userClient.getUserById(anyLong())).thenReturn(user);

        LoanApprovalRequestDTO req = new LoanApprovalRequestDTO(
                1L, "APPROVED",
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(10),
                "ok"
        );

        LoanApplicationDTO dto = service.updateLoanStatus(req, 100L);

        assertEquals(LoanStatus.APPROVED, dto.getStatus());
        verify(emiClient).generateEMISchedule(any()); 
    }

    @Test
    void getLoanById_notFound() {
        when(loanRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.getLoanById(1L));
    }

    @Test
    void getCustomerLoans_success() {
        LoanApplication loan = new LoanApplication();
        loan.setLoanTypeId(1L); 

        when(loanRepo.findByCustomerId(1L))
                .thenReturn(List.of(loan));

        assertEquals(1, service.getCustomerLoans(1L).size());
    }

    @Test
    void closeLoan_success() {
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setCustomerId(1L);

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));

        service.closeLoan(1L);

        assertEquals(LoanStatus.CLOSED, loan.getStatus());
    }

    @Test
    void updateLoanStatus_underReview_success() {
        LoanApplication loan = buildLoan(LoanStatus.APPLIED);

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        
        when(userClient.getUserById(anyLong())).thenReturn(new UserDTO(1L, "a@b.com", "User"));

        LoanApprovalRequestDTO req =
                new LoanApprovalRequestDTO(1L, "UNDER_REVIEW", null, null, "ok");

        LoanApplicationDTO dto = service.updateLoanStatus(req, 10L);

        assertEquals(LoanStatus.UNDER_REVIEW, dto.getStatus());
    }

    @Test
    void updateLoanStatus_rejected_success() {
        LoanApplication loan = buildLoan(LoanStatus.APPLIED);

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        
        when(userClient.getUserById(anyLong())).thenReturn(new UserDTO(1L, "a@b.com", "User"));

        LoanApprovalRequestDTO req =
                new LoanApprovalRequestDTO(1L, "REJECTED", null, null, "bad");

        LoanApplicationDTO dto = service.updateLoanStatus(req, 10L);

        assertEquals(LoanStatus.REJECTED, dto.getStatus());
    }

    @Test
    void updateLoanStatus_invalidStatus_throws() {
        LoanApplication loan = buildLoan(LoanStatus.APPLIED);

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));

        LoanApprovalRequestDTO req =
                new LoanApprovalRequestDTO(1L, "INVALID", null, null, "x");

        assertThrows(RuntimeException.class,
                () -> service.updateLoanStatus(req, 10L));
    }

    private LoanApplication buildLoan(LoanStatus status) {
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setLoanTypeId(1L); 
        loan.setStatus(status);
        loan.setTenure(12);
        loan.setCustomerId(1L);
        loan.setLoanAmount(BigDecimal.valueOf(100000));
        loan.setAnnualIncome(BigDecimal.valueOf(500000));

        return loan;
    }
    
    
}