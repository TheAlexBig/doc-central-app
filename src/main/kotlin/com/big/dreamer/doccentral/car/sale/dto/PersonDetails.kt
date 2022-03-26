package com.big.dreamer.doccentral.car.sale.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class PersonDetails(
    @JsonAlias("nombre")
    val givenName: String = "",
    @JsonAlias("apellido")
    val lastName: String = "",
    @JsonAlias("departamento")
    val state: String = "",
    @JsonAlias("domicilio")
    val settlement: String = "",
    @JsonAlias("documento")
    val document: String = "",
    val nit: String = "",
    @JsonAlias("genero")
    var gender: String = "",
    @JsonAlias("edad")
    val age: String = "",
    @JsonAlias("oficio")
    val job: String = "")
