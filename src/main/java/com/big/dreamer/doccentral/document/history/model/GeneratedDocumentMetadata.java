package com.big.dreamer.doccentral.document.history.model;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;

import java.util.Map;

public record GeneratedDocumentMetadata(
        String id,
        String type,
        String fileName,
        String createdAt,
        String title,
        String buyerName,
        String sellerName,
        String vehicle,
        CarSaleDocumentRequest document,
        Map<String, Object> draft,
        MutualDocumentRequest mutualDocument) {
}
