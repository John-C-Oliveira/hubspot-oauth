package service;

import com.example.oauth.model.HubspotWebhookEvent;
import com.example.oauth.service.impl.WebhookServiceImpl;
import com.example.oauth.util.HubspotWebhookVerifier;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

class WebhookServiceImplTest {

    @InjectMocks
    private WebhookServiceImpl webhookService;

    @Mock
    private HubspotWebhookVerifier verifier;

    @BeforeEach
     void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("sucesso ao registrar os eventos do Webhook")
     void testProcessEvents_ShouldLogEventsSuccessfully() throws SignatureException {
       HttpServletRequest request = mock(HttpServletRequest.class);
        HubspotWebhookEvent event1 = new HubspotWebhookEvent();
        event1.setSubscriptionType("contact");
        event1.setObjectId(123L);

        HubspotWebhookEvent event2 = new HubspotWebhookEvent();
        event2.setSubscriptionType("contact");
        event2.setObjectId(456L);

        List<HubspotWebhookEvent> events = Arrays.asList(event1, event2);



        webhookService.processEvents(request,events);

    }
}
