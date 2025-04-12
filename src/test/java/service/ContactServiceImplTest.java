package service;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.HubspotIntegrationException;
import com.example.oauth.model.ContactRequestDTO;
import com.example.oauth.repository.OAuthToken;
import com.example.oauth.repository.TokenRepository;
import com.example.oauth.service.OAuthService;
import com.example.oauth.service.impl.ContactServiceImpl;
import com.example.oauth.util.HttpUtil;
import com.example.oauth.util.TokenManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContactServiceImplTest {

    @Mock
    private OAuthService mockOAuthService;

    @Mock
    private HubSpotOAuthConfig mockConfig;

    @InjectMocks
    private ContactServiceImpl contactService;

    @Mock
    private TokenManager tokenManager;
    @Mock
    private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    @DisplayName("Deve criar uma contato com sucesso no HubSpot")
    void testCreateHubspotContact_SuccessfulResponse()   {
        OAuthToken mockToken = new OAuthToken();
        mockToken.setAccessToken("test-access-token");

        when(mockOAuthService.getValidTokenSync()).thenReturn(mockToken);
        when(mockConfig.getCreationContact()).thenReturn("https://api.hubspot.com/contacts/v1/contact");

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"id\":\"12345\"}");

        try (MockedStatic<HttpUtil> mockedHttpUtil = Mockito.mockStatic(HttpUtil.class)) {
            mockedHttpUtil.when(() -> HttpUtil.postJson(any(), any(), any())).thenReturn(mockResponse);

            ContactRequestDTO request = new ContactRequestDTO("john@gmail.com", "John", "Oliveira", "(84) 99999-9999");

            ResponseEntity<String> response = contactService.createHubspotContact(request);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals("{\"id\":\"12345\"}", response.getBody());

            verify(mockOAuthService, times(1)).getValidTokenSync();
            verifyNoMoreInteractions(mockOAuthService);
        }
    }

    @Test
    @DisplayName("deve gerar um erro de integração com o HubSpot")
    void testCreateHubspotContact_HubspotIntegrationError() {
        OAuthToken mockToken = new OAuthToken();
        mockToken.setAccessToken("test-access-token");

        when(mockOAuthService.getValidTokenSync()).thenReturn(mockToken);
        when(mockConfig.getCreationContact()).thenReturn("https://api.hubspot.com/contacts/v1/contact");

        try (MockedStatic<HttpUtil> mockedHttpUtil = Mockito.mockStatic(HttpUtil.class)) {
            mockedHttpUtil.when(() -> HttpUtil.postJson(any(), any(), any()))
                    .thenThrow(new RuntimeException("Connection error"));

            ContactRequestDTO request = new ContactRequestDTO("john@gmail.com", "John", "Oliveira", "(84) 99999-9999");

            Exception exception = assertThrows(HubspotIntegrationException.class, () -> contactService.createHubspotContact(request));

            assertTrue(exception.getMessage().contains("Erro ao se comunicar com a HubSpot"));
            verify(mockOAuthService, times(1)).getValidTokenSync();
        }
    }

    @Test
    @DisplayName("deve gerar um erro ao fazer o parser da resposta do HubSpot")
    void testCreateHubspotContact_ResponseParseError()   {
        OAuthToken mockToken = new OAuthToken();
        mockToken.setAccessToken("test-access-token");

        when(mockOAuthService.getValidTokenSync()).thenReturn(mockToken);
        when(mockConfig.getCreationContact()).thenReturn("https://api.hubspot.com/contacts/v1/contact");

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("Invalid JSON");

        try (MockedStatic<HttpUtil> mockedHttpUtil = Mockito.mockStatic(HttpUtil.class)) {
            mockedHttpUtil.when(() -> HttpUtil.postJson(any(), any(), any())).thenReturn(mockResponse);

            ContactRequestDTO request = new ContactRequestDTO("john@gmail.com", "John", "Oliveira", "(84) 99999-9999");

            Exception exception = assertThrows(HubspotIntegrationException.class, () -> contactService.createHubspotContact(request));

            assertTrue(exception.getMessage().contains("Resposta da HubSpot inválida"));
            verify(mockOAuthService, times(1)).getValidTokenSync();
        }
    }
    @Test
    @DisplayName("deve reutilizar token do cache e não acessar o banco")
    void shouldUseCachedTokenAndNotQueryDatabase() {
        OAuthToken token = new OAuthToken();
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresIn(3600);

        tokenManager.saveToken(token);

        OAuthToken result1 = tokenManager.getValidToken();
        OAuthToken result2 = tokenManager.getValidToken();

        assertSame(result1, result2);
        verify(tokenRepository, never()).findTopByOrderByCreatedAtDesc();
    }



}