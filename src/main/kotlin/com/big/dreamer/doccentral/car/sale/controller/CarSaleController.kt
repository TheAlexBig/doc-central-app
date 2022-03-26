package com.big.dreamer.doccentral.car.sale.controller

import com.big.dreamer.doccentral.car.sale.dto.CarSale
import com.big.dreamer.doccentral.car.sale.service.CarSaleService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets


@RestController
@RequestMapping("/v1/doc/generate")
class CarSaleController(val carSaleService: CarSaleService) {

    @PostMapping("/car-sale/word")
    fun getDocument(@RequestBody carSale: CarSale): ResponseEntity<ByteArray> {
            val output = ByteArrayOutputStream()
        carSaleService.createDocument(output, StandardCharsets.UTF_8, carSale)
        val httpHeaders = HttpHeaders()
        httpHeaders.add("Content-disposition", "attachment; filename=compra-venta.docx")
        return ResponseEntity.status(HttpStatus.CREATED)
            .headers(httpHeaders)
            .body(output.toByteArray())
    }
}