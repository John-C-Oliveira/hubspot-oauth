package service;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.HubspotIntegrationException;
import com.example.oauth.model.TokenResponseDTO;
import com.example.oauth.repository.OAuthToken;
import com.example.oauth.service.impl.OAuthServiceImpl;
import com.example.oauth.util.HttpUtil;
import com.example.oauth.util.TokenManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OAuthServiceImplTest {

    @Mock
    private HubSpotOAuthConfig config;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private OAuthServiceImpl oAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve retornar uma URL para fluxo oauth2 com HubSpot")
    void testGetAuthorizationUrl() {
        when(config.getAuthorizationUrl()).thenReturn("https://example.com/authorize");
        when(config.getClientId()).thenReturn("test-client-id");
        when(config.getRedirectUri()).thenReturn("https://example.com/callback");
        when(config.getScopes()).thenReturn("scope1 scope2");

        String expectedUrl = "https://example.com/authorize?client_id=test-client-id&redirect_uri=https%3A%2F%2Fexample.com%2Fcallback&scope=scope1+scope2";
        String actualUrl = oAuthService.getAuthorizationUrl();

        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    @DisplayName("Deve retornar sucesso ao fazer a troca do token com o HubSpot")
    void testExchangeCodeForTokenSuccess() throws JsonProcessingException {
        String tokenUrl = "https://example.com/token";
        when(config.getTokenUrl()).thenReturn(tokenUrl);

        String responseBody = """
                {
                  "access_token": "mock-access-token",
                  "refresh_token": "mock-refresh-token",
                  "expires_in": 3600
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(responseBody);

        try (var mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.postForm(eq(tokenUrl), any()))
                    .thenReturn(mockResponse);

            TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
            tokenResponseDTO.setAccessToken("mock-access-token");
            tokenResponseDTO.setRefreshToken("mock-refresh-token");
            tokenResponseDTO.setExpiresIn(3600);

            when(objectMapper.readValue(responseBody, TokenResponseDTO.class)).thenReturn(tokenResponseDTO);

            String result = oAuthService.exchangeCodeForToken("test-code");

            assertEquals("Token salvo com sucesso!", result);
            verify(tokenManager, times(1)).saveToken(any(OAuthToken.class));
        }
    }

    @Test
    @DisplayName("Deve gerar HubspotIntegrationException ao tentar fazer a troca de token com o HubSpot")
    void testExchangeCodeForTokenHttpError()   {
        String tokenUrl = "https://example.com/token";
        when(config.getTokenUrl()).thenReturn(tokenUrl);

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Erro ao trocar código por token");

        try (var mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.postForm(eq(tokenUrl), any()))
                    .thenReturn(mockResponse);

            Exception exception = assertThrows(HubspotIntegrationException.class, () -> oAuthService.exchangeCodeForToken("test-code"));

            assertTrue(exception.getMessage().contains("HubSpot retornou erro ao tentar trocar código por token."));
        }
    }

    @Test
    @DisplayName("deve gerar um IOException ao tentar fazer a troca de token com o HubSpot")
    void testExchangeCodeForTokenIOException()  {
        String tokenUrl = "https://example.com/token";
        when(config.getTokenUrl()).thenReturn(tokenUrl);

        try (var mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.postForm(eq(tokenUrl), any()))
                    .thenThrow(new IOException("Foi gerada um IOException"));

            Exception exception = assertThrows(HubspotIntegrationException.class, () -> oAuthService.exchangeCodeForToken("test-code"));
            assertTrue(exception.getMessage().contains("Erro de comunicação com a HubSpot ao tentar trocar o código por token."));

            mockedStatic.verify(() -> HttpUtil.postForm(eq(tokenUrl), any()), times(1));
        }
    }

    @Test
    @DisplayName("deve validar o token")
    void testGetValidTokenSync() {
        OAuthToken mockToken = new OAuthToken();
        when(tokenManager.getValidToken()).thenReturn(mockToken);

        OAuthToken result = oAuthService.getValidTokenSync();

        assertEquals(mockToken, result);
    }
}
