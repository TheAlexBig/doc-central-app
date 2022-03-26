package com.big.dreamer.doccentral.car.sale.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class LegalAgentDetails (
    @JsonAlias("nombre")
    val givenName: String = "",
    @JsonAlias("apellido")
    val lastName: String = "",
    @JsonAlias("departamento")
    val state: String = "",
    @JsonAlias("domicilio")
    val settlement: String = "",
    @JsonAlias("genero")
    var gender: String = ""
)