package com.secondHand.SecondHandMarket;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordTest {
    @Test
    void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("admin1234");
        System.out.println("@@@ 이 값을 DB에 넣으세요: " + encoded);
    }
}