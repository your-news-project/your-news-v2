package kr.co.yournews.infra.oauth.oidc;

import kr.co.yournews.common.response.error.type.GlobalErrorType;
import kr.co.yournews.common.response.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OAuthOidcHttpClient {

    /**
     * 토큰 발급 요청을 전송하고, 응답으로 받은 JSON 문자열을 반환하는 메서드
     */
    String getTokenResponse(String tokenUri, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = new RestTemplate().exchange(
                    tokenUri,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new CustomException(GlobalErrorType.INTERNAL_SERVER_ERROR);
        }
    }
}
