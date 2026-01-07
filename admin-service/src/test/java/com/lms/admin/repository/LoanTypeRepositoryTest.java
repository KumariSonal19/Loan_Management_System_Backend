//package com.lms.admin.repository;
//
//import com.lms.admin.entity.LoanType;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//class LoanTypeRepositoryTest {
//
//    @Autowired
//    private LoanTypeRepository loanTypeRepository;
//
//    @Test
//    void findByTypeName_success() {
//        LoanType loanType = new LoanType();
//        loanType.setTypeName("Personal Loan");
//        loanType.setMinAmount(BigDecimal.valueOf(50000));
//        loanType.setMaxAmount(BigDecimal.valueOf(500000));
//        loanType.setBaseInterestRate(BigDecimal.valueOf(12));
//        loanType.setMinTenure(6);
//        loanType.setMaxTenure(60);
//        loanType.setIsActive(true);
//
//        loanTypeRepository.save(loanType);
//
//        Optional<LoanType> result =
//                loanTypeRepository.findByTypeName("Personal Loan");
//
//        assertTrue(result.isPresent());
//        assertEquals("Personal Loan", result.get().getTypeName());
//    }
//
//    @Test
//    void existsByTypeNameAndIdNot_success() {
//        LoanType loanType = loanTypeRepository.save(new LoanType(
//                null,
//                "Car Loan",
//                BigDecimal.valueOf(100000),
//                BigDecimal.valueOf(1000000),
//                BigDecimal.valueOf(9),
//                12,
//                84,
//                null,
//                true,
//                null,
//                null,
//                0L
//        ));
//
//        boolean exists = loanTypeRepository
//                .existsByTypeNameAndIdNot("Car Loan", loanType.getId());
//
//        assertFalse(exists);
//    }
//}
