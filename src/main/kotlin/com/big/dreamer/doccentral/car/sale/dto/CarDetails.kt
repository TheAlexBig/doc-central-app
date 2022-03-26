package com.big.dreamer.doccentral.car.sale.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class CarDetails(
    @JsonAlias("placa")
    val licensePlate: String = "",
    @JsonAlias("marca")
    val brand: String = "",
    @JsonAlias("modelo")
    val model: String = "",
    val color: String = "",
    @JsonAlias("fabricado")
    val factoryYear: String = "",
    @JsonAlias("capacidad")
    val capacity: String = "",
    @JsonAlias("dominio")
    val domain: String = "",
    @JsonAlias("clase")
    val vehicleClass: String = "",
    @JsonAlias("num_motor")
    val engineNumber: String = "",
    @JsonAlias("num_chasis")
    val chassisNumber: String = "",
    @JsonAlias("num_vin")
    val vinNumber: String = "")