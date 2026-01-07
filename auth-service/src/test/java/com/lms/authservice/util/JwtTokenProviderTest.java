package com.lms.authservice.util;

import com.lms.authservice.entity.Role;
import com.lms.authservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=ThisIsAVeryLongAndSecureSecretKeyThatIsAtLeastSixtyFourCharactersLong",
    "jwt.expiration=3600000"
})
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void generateToken_shouldWork() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setRole(Role.CUSTOMER);

        String token = jwtTokenProvider.generateToken(user);

        assertNotNull(token);
    }

    @Test
    void validateToken_shouldReturnTrue() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setRole(Role.CUSTOMER);

        String token = jwtTokenProvider.generateToken(user);

        assertTrue(jwtTokenProvider.validateToken(token));
    }
}
