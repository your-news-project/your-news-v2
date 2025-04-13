package kr.co.yournews.apis.auth.service;

import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.service.BasicAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthCommandService {
    private final BasicAuthService basicAuthService;

    @Transactional
    public void signUp(SignUpDto.Auth signUpRequest) {
        basicAuthService.createUser(signUpRequest);
    }
}
