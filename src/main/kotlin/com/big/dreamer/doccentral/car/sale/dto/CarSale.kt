package com.big.dreamer.doccentral.car.sale.dto

import com.fasterxml.jackson.annotation.JsonAlias

class CarSale(
    @JsonAlias("vendedor")
    val seller : PersonDetails = PersonDetails(),
    @JsonAlias("comprador")
    val buyer : PersonDetails = PersonDetails(),
    @JsonAlias("vehiculo")
    val vehicle: CarDetails = CarDetails(),
    @JsonAlias("documento")
    val document: DocumentDetails = DocumentDetails(),
    @JsonAlias("agente_juridico")
    val legalAgent : LegalAgentDetails = LegalAgentDetails()
    )

