package com.lms.emi.service;

import com.lms.emi.dto.EMIScheduleDTO;
import com.lms.emi.dto.RepaymentDTO;
import com.lms.emi.entity.EMISchedule;
import com.lms.emi.entity.EMIStatus;
import com.lms.emi.entity.PaymentMode;
import com.lms.emi.entity.Repayment;
import com.lms.emi.repository.EMIScheduleRepository;
import com.lms.emi.repository.RepaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EMIServiceTest {

    @InjectMocks
    private EMIService emiService;

    @Mock
    private EMIScheduleRepository emiScheduleRepository;

    @Mock
    private RepaymentRepository repaymentRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateEMISchedule_success() {
        when(emiScheduleRepository.findByLoanApplicationId(1L))
                .thenReturn(Collections.emptyList());

        when(emiScheduleRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<EMIScheduleDTO> result = emiService.generateEMISchedule(
                1L,
                new BigDecimal("100000"),
                new BigDecimal("12"),
                12,
                LocalDate.now()
        );

        assertEquals(12, result.size());
        assertEquals(EMIStatus.PENDING, result.get(0).getStatus());
        verify(emiScheduleRepository, times(12)).save(any());
    }

    @Test
    void generateEMISchedule_alreadyExists_throwsException() {
        when(emiScheduleRepository.findByLoanApplicationId(1L))
                .thenReturn(List.of(new EMISchedule()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emiService.generateEMISchedule(
                        1L,
                        BigDecimal.TEN,
                        BigDecimal.TEN,
                        10,
                        LocalDate.now()
                ));

        assertEquals("EMI schedule already exists for this loan", ex.getMessage());
    }

    @Test
    void recordRepayment_success() {
        EMISchedule schedule = new EMISchedule();
        schedule.setId(1L);
        schedule.setEmiAmount(new BigDecimal("5000"));
        schedule.setStatus(EMIStatus.PENDING);

        when(emiScheduleRepository.findById(1L))
                .thenReturn(Optional.of(schedule));

        when(repaymentRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RepaymentDTO dto = emiService.recordRepayment(
                1L,
                new BigDecimal("5000"),
                PaymentMode.UPI
        );

        assertEquals("UPI", dto.getPaymentMode());
        assertNotNull(dto.getTransactionId());
        verify(emiScheduleRepository).save(schedule);
    }

    @Test
    void recordRepayment_insufficientAmount_throwsException() {
        EMISchedule schedule = new EMISchedule();
        schedule.setEmiAmount(new BigDecimal("5000"));
        schedule.setStatus(EMIStatus.PENDING);

        when(emiScheduleRepository.findById(1L))
                .thenReturn(Optional.of(schedule));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emiService.recordRepayment(
                        1L,
                        new BigDecimal("1000"),
                        PaymentMode.ONLINE
                ));

        assertTrue(ex.getMessage().contains("Insufficient Amount"));
    }

    @Test
    void getOutstandingBalance_success() {
        EMISchedule s1 = new EMISchedule();
        s1.setEmiAmount(new BigDecimal("3000"));
        s1.setStatus(EMIStatus.PENDING);

        EMISchedule s2 = new EMISchedule();
        s2.setEmiAmount(new BigDecimal("2000"));
        s2.setStatus(EMIStatus.PAID);

        when(emiScheduleRepository.findByLoanApplicationIdOrderByEmiNumber(1L))
                .thenReturn(List.of(s1, s2));

        BigDecimal balance = emiService.getOutstandingBalance(1L);
        assertEquals(new BigDecimal("3000.00"), balance);
    }

    @Test
    void getOverdueEMIs_success() {
        EMISchedule overdue = new EMISchedule();
        overdue.setStatus(EMIStatus.PENDING);
        overdue.setDueDate(LocalDate.now().minusDays(1));

        when(emiScheduleRepository.findByStatusAndDueDateBefore(
                EMIStatus.PENDING, LocalDate.now()))
                .thenReturn(List.of(overdue));

        List<EMIScheduleDTO> result = emiService.getOverdueEMIs();
        assertEquals(1, result.size());
    }
}
