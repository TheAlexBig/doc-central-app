package com.big.dreamer.doccentral.document.mutual.model;

import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record MutualDocumentRequest(
        @JsonProperty("deudor") @Valid @NotNull PersonDetails debtor,
        @JsonProperty("acreedor") @Valid @NotNull PersonDetails creditor,
        @JsonProperty("fiador") @Valid PersonDetails guarantor,
        @JsonProperty("garantia_prendaria") @Valid MutualVehiclePledge vehiclePledge,
        @JsonProperty("condiciones") @Valid @NotNull MutualTerms terms,
        @JsonProperty("agente_juridico") @Valid @NotNull LegalAgentDetails legalAgent) {

    public MutualDocumentRequest(PersonDetails debtor, PersonDetails creditor, MutualTerms terms,
                                 LegalAgentDetails legalAgent) {
        this(debtor, creditor, null, null, terms, legalAgent);
    }

    public MutualDocumentRequest(PersonDetails debtor, PersonDetails creditor, PersonDetails guarantor,
                                 MutualTerms terms, LegalAgentDetails legalAgent) {
        this(debtor, creditor, guarantor, null, terms, legalAgent);
    }
}
