package controller;

import com.example.oauth.controller.OAuthController;
import com.example.oauth.service.OAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

 class OAuthControllerTest {

    @Mock
    private OAuthService oAuthService;
    @InjectMocks
    private OAuthController oAuthController;

    @BeforeEach
     void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("deve gerar uma URL para autorização com o HubSpot")
     void testGenerateAuthorizationUrl() {
        String expectedUrl = "https://auth.hubspot.com/authorize?client_id=123";

        when(oAuthService.getAuthorizationUrl()).thenReturn(expectedUrl);

        String result = oAuthController.generateAuthorizationUrl();

        assertEquals(expectedUrl, result);
        verify(oAuthService, times(1)).getAuthorizationUrl();
    }

    @Test
    @DisplayName("deve receber uma chamada do callback com sucesso")
     void testOauthCallback_Success() {
        String code = "auth-code-123";
        String expectedMsg = "Token recebido com sucesso";

        when(oAuthService.exchangeCodeForToken(code)).thenReturn(expectedMsg);

        ResponseEntity<String> response = oAuthController.oauthCallback(code, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMsg, response.getBody());
    }

    @Test
    @DisplayName("deve receber uma chamada do callback com erro do HubSpot")
     void testOauthCallback_HubspotError() {
        String error = "access_denied";

        ResponseEntity<String> response = oAuthController.oauthCallback(null, error);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Erro recebido do HubSpot"));
    }

    @Test
    @DisplayName("deve receber um erro por não obter o código de autorização")
     void testOauthCallback_MissingCode() {
        ResponseEntity<String> response = oAuthController.oauthCallback("", null);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Código de autorização não informado"));
    }


}
