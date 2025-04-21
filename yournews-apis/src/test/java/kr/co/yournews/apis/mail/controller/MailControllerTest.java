package kr.co.yournews.apis.mail.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.auth.mail.controller.MailController;
import kr.co.yournews.apis.auth.mail.dto.InquiryMailReq;
import kr.co.yournews.apis.auth.mail.service.InquiryMailer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MailController.class)
public class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InquiryMailer inquiryMailer;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    @DisplayName("이메일 전송 성공")
    void sendInquirySuccess() throws Exception {
        // given
        InquiryMailReq request = new InquiryMailReq("test@example.com", "문의 내용입니다.");

        doNothing().when(inquiryMailer).sendInquiryMail(request);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/mail/inquiry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }

    @Test
    @DisplayName("이메일 전송 실패 - 메시지 누락")
    void sendInquiryFail_missingMessage() throws Exception {
        // given
        InquiryMailReq request = new InquiryMailReq("test@example.com", null);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/mail/inquiry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").value("문의 내용을 입력해주세요."));
    }
}
