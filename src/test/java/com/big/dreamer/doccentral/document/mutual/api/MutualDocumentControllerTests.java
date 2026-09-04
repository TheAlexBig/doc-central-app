package com.big.dreamer.doccentral.document.mutual.api;

import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.document.history.model.MutualGenerationRequest;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.model.MutualTerms;
import com.big.dreamer.doccentral.document.mutual.service.MutualDocumentService;
import com.big.dreamer.doccentral.license.service.LicenseService;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MutualDocumentControllerTests {

    @Test
    void trackedGenerationRegistersMutualInHistory() {
        MutualDocumentService service = mock(MutualDocumentService.class);
        GeneratedDocumentStorage storage = mock(GeneratedDocumentStorage.class);
        GeneratedDocumentHistoryRepository history = mock(GeneratedDocumentHistoryRepository.class);
        when(service.createDocument(any())).thenReturn(new byte[]{1, 2, 3});
        when(history.saveMutual(any(), any(), any(), any())).thenReturn(new GeneratedDocumentMetadata(
                "history-id", "mutual", "mutuo.docx", "2026-09-04T00:00:00Z",
                "Mutuo", "Deudor", "Acreedor", "", null, Map.of(), request()));
        MutualDocumentController controller = new MutualDocumentController(
                service, storage, mock(LicenseService.class), history);

        var response = controller.generateTracked(
                new MutualGenerationRequest(request(), Map.of("terms", Map.of("amount", "750"))),
                "docx");

        assertThat(response.getHeaders().getFirst("X-Document-History-Id")).isEqualTo("history-id");
        assertThat(response.getBody()).containsExactly(1, 2, 3);
        verify(history).saveMutual(any(), any(), any(), any());
    }

    private MutualDocumentRequest request() {
        PersonDetails debtor = new PersonDetails("Ana", "Deudora", "La Libertad", "Santa Tecla",
                "UNO", "Femenino", "TREINTA", "Comerciante");
        PersonDetails creditor = new PersonDetails("Luis", "Acreedor", "San Salvador", "San Salvador",
                "DOS", "Masculino", "CUARENTA", "Empleado");
        MutualTerms terms = new MutualTerms("SETECIENTOS CINCUENTA", "SEIS MESES", "FECHA",
                "SEIS", "CIENTO VEINTICINCO", "BANCO", "CUENTA", "", "", "CONSUMO",
                false, "", "", "SANTA TECLA", "SANTA TECLA", "LA LIBERTAD", "FECHA", "HORA", "No", "No");
        LegalAgentDetails notary = new LegalAgentDetails("Nora", "Notaria", "San Salvador",
                "San Salvador", "Femenino", "Notario");
        return new MutualDocumentRequest(debtor, creditor, terms, notary);
    }
}
