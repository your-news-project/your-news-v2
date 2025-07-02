package kr.co.yournews.infra.oauth.basic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class OAuthHttpClient {
    private final ObjectMapper objectMapper;

    /**
     * 플랫폼의 access token을 받아오는 메서드
     *
     * @param tokenUri : token을 받아오기 위한 uri
     * @param params   : token을 받아오기 위한 파라미터
     * @return : 플랫폼의 accessToken
     */
    String getAccessToken(String tokenUri, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("access_token").asText();
        } catch (Exception e) {
            throw new CustomException(GlobalErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 플랫폼의 access token을 통해 사용자 정보 불러오는 메서드
     *
     * @param userInfoUri : 사용자 정보를 받아오기 위한 uri
     * @param accessToken : 플랫폼의 access token
     * @return : 플랫폼 사용자 정보
     */
    JsonNode getUserInfo(String userInfoUri, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    String.class
            );
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new CustomException(GlobalErrorType.INTERNAL_SERVER_ERROR);
        }
    }
}
