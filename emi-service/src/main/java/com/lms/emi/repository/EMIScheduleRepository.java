package com.lms.emi.repository;

import com.lms.emi.entity.EMISchedule;
import com.lms.emi.entity.EMIStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EMIScheduleRepository extends JpaRepository<EMISchedule, Long> {

    List<EMISchedule> findByLoanApplicationId(Long loanApplicationId);

    List<EMISchedule> findByLoanApplicationIdOrderByEmiNumber(Long loanApplicationId);

    List<EMISchedule> findByStatus(EMIStatus status);

    List<EMISchedule> findByStatusAndDueDateBefore(EMIStatus status, LocalDate date);

    long countByLoanApplicationId(Long loanApplicationId);
    
    @Query("SELECT COALESCE(SUM(e.emiAmount), 0) FROM EMISchedule e WHERE e.status = 'PENDING' AND e.loanApplicationId IN :loanIds")
    BigDecimal sumPendingAmountByLoanIds(@Param("loanIds") List<Long> loanIds);
}
