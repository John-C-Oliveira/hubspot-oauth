package controller;

import com.example.oauth.controller.ContactController;
import com.example.oauth.model.ContactRequestDTO;
import com.example.oauth.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

 class ContactControllerTest {

    @InjectMocks
    private ContactController contactController;
    @Mock
    private ContactService contactService;

    @BeforeEach
     void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("deve criar uma contato com sucesso no HubSpot")
     void testCreateContact_Success() {
        ContactRequestDTO request = new ContactRequestDTO();
        request.setEmail("exemplo@teste.com");

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Contato criado com sucesso");

        when(contactService.createHubspotContact(request)).thenReturn(expectedResponse);

        ResponseEntity<String> response = contactController.createContact(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Contato criado com sucesso", response.getBody());
        verify(contactService, times(1)).createHubspotContact(request);
    }


}
