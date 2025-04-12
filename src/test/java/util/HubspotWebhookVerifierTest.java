package util;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.model.HubspotWebhookEvent;
import com.example.oauth.util.HubspotWebhookVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HubspotWebhookVerifierTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HubSpotOAuthConfig config;

    @InjectMocks
    private HubspotWebhookVerifier verifier;

    @Mock
    private HubspotWebhookEvent event;
    @Mock
    private List<HubspotWebhookEvent> events;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        verifier = new HubspotWebhookVerifier(config);

        event = new HubspotWebhookEvent();
        event.setAppId(10661809L);
        event.setEventId(100L);
        event.setSubscriptionId(3447522L);
        event.setPortalId(49650143L);
        event.setOccurredAt(1744439068495L);
        event.setSubscriptionType("contact.creation");
        event.setAttemptNumber(0L);
        event.setObjectId(123L);
        event.setChangeSource("CRM");
        event.setChangeFlag("NEW");

        events = List.of(event);
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @DisplayName("Validação com assinatura v1 correta")
    @ValueSource(strings = {"v1", "v2","v3"})
    void testValidRequestV1(String version) throws Exception {

        String secret = "client-secret";
        String body = objectMapper.writeValueAsString(events);
        String hubSpotSignature;
        when(config.getClientSecret()).thenReturn(secret);

        switch (version) {
            case "v3" -> {
                String expectedSignature = generateSha256Hash(secret + body);

                when(config.getClientSecret()).thenReturn(secret);
                when(request.getHeader("X-HubSpot-Signature-version")).thenReturn("v3");
                when(request.getHeader("X-HubSpot-Signature-v3")).thenReturn(expectedSignature);

                assertThrows(SignatureException.class,() -> verifier.isValidRequest(request, events));

                verify(request).getHeader("X-HubSpot-Signature-version");
                verify(request,times(1)).getHeader("X-HubSpot-Signature-v3");

            }
            case "v2" -> {
                String expectedSignature = generateSha256Hash(secret + body);

                when(config.getClientSecret()).thenReturn(secret);
                when(request.getHeader("X-HubSpot-Signature-version")).thenReturn("v2");
                when(request.getHeader("X-HubSpot-Signature-v2")).thenReturn(expectedSignature);

                assertThrows(SignatureException.class,() -> verifier.isValidRequest(request, events));

                verify(request).getHeader("X-HubSpot-Signature-version");
                verify(request,times(1)).getHeader("X-HubSpot-Signature-v2");


            }
            case "v1" -> {
                String expectedSignature = generateSha256Hash(secret + body);

                when(config.getClientSecret()).thenReturn(secret);
                when(request.getHeader("X-HubSpot-Signature-version")).thenReturn("v1");
                when(request.getHeader("X-HubSpot-Signature")).thenReturn(expectedSignature);

                assertDoesNotThrow(() -> verifier.isValidRequest(request, events));

                verify(request).getHeader("X-HubSpot-Signature-version");
                verify(request,times(1)).getHeader("X-HubSpot-Signature");
                verify(config).getClientSecret();

            }
            default -> {
                throw new SignatureException("Assinatura ausente");
            }
        }





    }

    @Test
    @DisplayName("Assinatura ausente deve lançar exceção")
    void testInvalidSignature() throws Exception {
        String secret = "client-secret";
        when(config.getClientSecret()).thenReturn(secret);
        when(request.getHeader("X-HubSpot-Signature-version")).thenReturn("v1");
        when(request.getHeader("X-HubSpot-Signature")).thenReturn("assinatura-errada");

        SignatureException thrown = assertThrows(SignatureException.class, () -> verifier.isValidRequest(request, events));
        assertEquals("Assinatura ausente", thrown.getMessage());

        verify(request).getHeader("X-HubSpot-Signature");
        verify(config).getClientSecret();
    }

    @Test
    @DisplayName("Versão ausente no header deve lançar exceção")
    void testMissingVersionHeader() {
        when(request.getHeader("X-HubSpot-Signature-version")).thenReturn(null);

        SignatureException thrown = assertThrows(SignatureException.class, () -> verifier.isValidRequest(request, events));
        assertEquals("Assinatura ausente", thrown.getMessage());

        verify(request).getHeader("X-HubSpot-Signature-version");
    }

    @Test
    @DisplayName("Erro interno (ex: erro de header) deve gerar exceção de assinatura ausente")
    void testExceptionHandling() {
        when(request.getHeader(anyString())).thenThrow(new RuntimeException("erro inesperado"));

        SignatureException thrown = assertThrows(SignatureException.class, () -> verifier.isValidRequest(request, events));
        assertEquals("Assinatura ausente", thrown.getMessage());

        verify(request).getHeader("X-HubSpot-Signature-version");
    }

    private String generateSha256Hash(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


}
