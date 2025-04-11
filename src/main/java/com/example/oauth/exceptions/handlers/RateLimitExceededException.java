package com.example.oauth.exceptions.handlers;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class RateLimitExceededException extends HttpClientErrorException {

    public RateLimitExceededException(long retryAfterSeconds) {
        super(HttpStatusCode.valueOf(429),"Rate limit excedido. Tente novamente em " + retryAfterSeconds + " segundos.");
    }


}
