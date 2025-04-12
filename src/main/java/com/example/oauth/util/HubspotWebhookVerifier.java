package com.example.oauth.util;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.HubspotIntegrationException;
import com.example.oauth.model.HubspotWebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubspotWebhookVerifier {

    private final HubSpotOAuthConfig config;

    public void isValidRequest(HttpServletRequest request, List<HubspotWebhookEvent> events) throws SignatureException {
        log.info("Validando assinatura do webhook");
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();

            ObjectMapper objectMapper = new ObjectMapper();
            String eventJson = objectMapper.writeValueAsString(events);

            String signatureVersion = request.getHeader("X-HubSpot-Signature-version");

            String hubSpotSignature;

            switch (signatureVersion) {
                case "v3" -> {
                    hubSpotSignature = request.getHeader("X-HubSpot-Signature-v3");
                    String timestamp = request.getHeader("X-HubSpot-Request-Timestamp");
                    String sourceString = config.getClientSecret() + method + uri + eventJson + timestamp;
                    String expectedSignature = generateSha256Hash(sourceString);
                    checkSignatureValidate(hubSpotSignature, expectedSignature);
                }
                case "v2" -> {
                    hubSpotSignature = request.getHeader("X-HubSpot-Signature-v2");
                    String sourceString = config.getClientSecret() + method + uri + eventJson;
                    String expectedSignature = generateSha256Hash(sourceString);
                    checkSignatureValidate(hubSpotSignature, expectedSignature);
                }
                case "v1" -> {
                    hubSpotSignature = request.getHeader("X-HubSpot-Signature");
                    String sourceString = config.getClientSecret() + eventJson;
                    String expectedSignature = generateSha256Hash(sourceString);
                    checkSignatureValidate(hubSpotSignature, expectedSignature);
                }
                default -> {
                    log.warn("Nenhum cabeçalho de assinatura da HubSpot encontrado");
                    throw new SignatureException("Assinatura ausente");
                }
            }

        } catch (Exception e) {
            log.error("Erro ao validar assinatura do webhook. Motivo: [{}]", e.getMessage());
            throw new SignatureException("Assinatura ausente");
        }
    }

    private static void checkSignatureValidate(String hubSpotSignature, String expectedSignature) throws SignatureException {
        if (!hubSpotSignature.equals(expectedSignature)) {
            throw new SignatureException("Assinatura inválida");
        }
    }


    protected String generateSha256Hash(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new HubspotIntegrationException(e.getMessage());
        }
    }
}
