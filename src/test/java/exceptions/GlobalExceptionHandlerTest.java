package exceptions;

import com.example.oauth.exceptions.handlers.*;
import com.example.oauth.model.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {
    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleHubspotError() {
        HubspotIntegrationException ex = new HubspotIntegrationException("Hubspot error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleHubspotError(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Hubspot error", response.getBody().message());
        assertEquals("Bad Gateway", response.getBody().error());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void testHandleTokenNotFound() {
        TokenNotFoundException ex = new TokenNotFoundException("Token not found");
        ResponseEntity<ApiError> response = handler.handleTokenNotFound(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token not found", response.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
    }

    @Test
    void testHandleTokenRefresh() {
        TokenRefreshException ex = new TokenRefreshException("Token refresh failed");
        ResponseEntity<ApiError> response = handler.handleTokenRefresh(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token refresh failed", response.getBody().getMessage());
    }

    @Test
    void testHandleInvalidPayload() {
        InvalidPayloadException ex = new InvalidPayloadException("Invalid payload");
        ResponseEntity<ApiError> response = handler.handleInvalidPayload(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid payload", response.getBody().getMessage());
    }


    @Test
    void testHandleGeneric() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ApiError> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("Erro inesperado: Unexpected error"));
    }

    @Test
    void testHandleContactAlreadyExists() {
        ContactAlreadyExistsException ex = new ContactAlreadyExistsException("Contact already exists");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleContactAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Contact already exists", response.getBody().message());
    }

    @Test
    void testHandleSignatureInvalidException() {
        SignatureInvalidException ex = new SignatureInvalidException("Invalid signature");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleSignatureInvalidaException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().error());
        assertEquals("Invalid signature", response.getBody().message());
    }
}
