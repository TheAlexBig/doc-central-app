package com.big.dreamer.doccentral.car.sale.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class DocumentDetails(
    @JsonAlias("calidad_de")
    val garment: String = "",
    @JsonAlias("institucion")
    var institution: String = "",
    @JsonAlias("precio")
    val price: String = "",
    @JsonAlias("domicilio")
    val settlement: String = "",
    @JsonAlias("departamento")
    val state: String = "",
    @JsonAlias("fecha_firma")
    val signDate: String = "",
    @JsonAlias("hora_firma")
    val signHour: String = "",
    @JsonAlias("identifica_vendedor")
    var identifiesSeller: String = "No",
    @JsonAlias("identifica_comprador")
    var identifiesBuyer: String = "No",
)