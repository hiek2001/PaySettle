package org.project.paysystem.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 자동 rollback 설정
public class KakaoServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void 카카오_회원가입_성공() throws Exception {
        // given

    }

}
