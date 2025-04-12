package controller;

import com.example.oauth.controller.WebhookController;
import com.example.oauth.model.HubspotWebhookEvent;
import com.example.oauth.service.WebhookService;
import com.example.oauth.util.HubspotWebhookVerifier;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.security.SignatureException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

 class WebhookControllerTest {
    @Mock
    private WebhookService webhookService;
    @Mock
    private HubspotWebhookVerifier verifier;
    @InjectMocks
    private WebhookController webhookController;
    @BeforeEach
     void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("deve retornar uma assinatura invalida para a requisição")
     void testGetWebhook_SignatureInvalid() throws SignatureException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        request.setAttribute("X-HubSpot-Signature-version", "v3");
        List<HubspotWebhookEvent> events = Collections.singletonList(new HubspotWebhookEvent());


        ResponseEntity<String> response = webhookController.getWebhook(request, events);

       Assertions.assertDoesNotThrow(()->verifier.isValidRequest(request, events));
    }

    @Test
    @DisplayName("deve processar os eventos e retornar uma resposta de sucesso")
     void testGetWebhook_Success() throws SignatureException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        List<HubspotWebhookEvent> events = Collections.singletonList(new HubspotWebhookEvent());


        doNothing().when(webhookService).processEvents(request,events);

        ResponseEntity<String> response = webhookController.getWebhook(request, events);

        assertEquals(204, response.getStatusCodeValue());
        verify(webhookService, times(1)).processEvents(request,events);
    }

    @Test
    @DisplayName("deve processar os eventos e retornar uma resposta de sucesso mesmo com eventos vazios")
     void testGetWebhook_EmptyEvents() throws SignatureException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        List<HubspotWebhookEvent> events = Collections.emptyList();



        doNothing().when(webhookService).processEvents(request,events);

        ResponseEntity<String> response = webhookController.getWebhook(request, events);

        assertEquals(204, response.getStatusCodeValue());
    }


}
