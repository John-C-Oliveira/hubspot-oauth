package util;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.TokenNotFoundException;
import com.example.oauth.exceptions.handlers.TokenRefreshException;
import com.example.oauth.model.TokenResponseDTO;
import com.example.oauth.repository.OAuthToken;
import com.example.oauth.repository.TokenRepository;
import com.example.oauth.util.HttpUtil;
import com.example.oauth.util.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenManagerTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HubSpotOAuthConfig config;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("deve salvar um token no banco de dados")
    void shouldSaveTokenSuccessfully() {
        OAuthToken token = new OAuthToken();
        tokenManager.saveToken(token);
        verify(tokenRepository, times(1)).save(token);
    }

    @Test
    @DisplayName("deve retornar um token valido do banco")
    void shouldReturnValidTokenFromCache() {
        OAuthToken token = new OAuthToken();
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresIn(3600);
        tokenManager.saveToken(token);

        when(tokenRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(token));
        OAuthToken result = tokenManager.getValidToken();

        assertEquals(token, result);

        OAuthToken cached = tokenManager.getValidToken();
        assertEquals(result, cached);
    }

    @Test
    @DisplayName("deve retornar um erro por falta de token no banco")
    void shouldThrowTokenNotFoundExceptionWhenNoTokenExists() {
        when(tokenRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class, () -> tokenManager.getValidToken());
    }

    @Test
    @DisplayName("deve retornar um erro por token expirado no banco")
    void shouldRefreshTokenWhenExpired() throws Exception {
        OAuthToken expiredToken = new OAuthToken();
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        expiredToken.setExpiresIn(3600);
        expiredToken.setRefreshToken("refresh_token");
        expiredToken.setAccessToken("old_access");

        TokenResponseDTO refreshed = new TokenResponseDTO();
        refreshed.setAccessToken("new_access");
        refreshed.setExpiresIn(3600);

        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(tokenRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(expiredToken));
        when(config.getClientId()).thenReturn("client_id");
        when(config.getClientSecret()).thenReturn("secret");
        when(config.getTokenUrl()).thenReturn("https://api.hubspot.com/oauth/v1/token");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"access_token\":\"new_access\",\"expires_in\":3600}");

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.postForm(anyString(), anyString()))
                    .thenReturn(mockResponse);

            when(objectMapper.readValue(anyString(), eq(TokenResponseDTO.class)))
                    .thenReturn(refreshed);

            OAuthToken result = tokenManager.getValidToken();

            assertEquals("new_access", result.getAccessToken());
            verify(tokenRepository).save(expiredToken);
        }
    }

    @Test
    @DisplayName("deve retornar um erro por falha ao tentar fazer a troca de token")
    void shouldThrowTokenRefreshExceptionOnHttpError()   {
        OAuthToken expiredToken = new OAuthToken();
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        expiredToken.setExpiresIn(3600);
        expiredToken.setRefreshToken("refresh_token");

        when(tokenRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(expiredToken));
        when(config.getClientId()).thenReturn("client_id");
        when(config.getClientSecret()).thenReturn("secret");
        when(config.getTokenUrl()).thenReturn("https://api.hubspot.com/oauth/v1/token");

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Bad Request");

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.postForm(anyString(), anyString()))
                    .thenReturn(mockResponse);

            assertThrows(TokenRefreshException.class, () -> tokenManager.getValidToken());
        }
    }
}
