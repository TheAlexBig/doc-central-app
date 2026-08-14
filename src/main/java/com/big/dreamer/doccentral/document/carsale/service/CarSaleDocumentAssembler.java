package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.template.CarSaleTemplateRepository;

import java.util.Locale;

final class CarSaleDocumentAssembler {

    private static final String BUYER_DEFAULT = "EL COMPRADOR";
    private static final String BUYER_WOMAN = "LA COMPRADORA";
    private static final String SELLER_DEFAULT = "EL VENDEDOR";
    private static final String SELLER_WOMAN = "LA VENDEDORA";
    private static final String LEGAL_DEFAULT = "NOTARIO";
    private static final String LEGAL_WOMAN = "NOTARIA";
    private static final String LAWYER_DEFAULT = "ABOGADO";
    private static final String LAWYER_WOMAN = "ABOGADA";

    CarSaleDocumentSections createSections(
            CarSaleDocumentRequest request,
            CarSaleTemplateRepository.Templates templates) {
        String buyerTitle = buyerTitle(request.buyer());
        String sellerTitle = sellerTitle(request.seller());
        String declarationPeople = populatePeople(
                templates.peopleDocument(),
                request.seller(),
                sellerTitle,
                request.buyer(),
                buyerTitle);
        String declarationCar = populateCar(
                templates.carDocument(), request.vehicle(), request.seller());
        String declarationTerms = populateDocument(
                templates.document() + templates.firstSectionEnd(),
                request.document(), request.seller(), request.buyer());

        String legalAgent = populateLegalAgent(templates.legalAuthentic(), request.legalAgent(), request.document());
        String authenticPeople = populatePeople(
                templates.peopleAuthentic(),
                request.seller(),
                sellerTitle,
                request.buyer(),
                buyerTitle);
        authenticPeople = replaceFirst(
                authenticPeople,
                ":identifiesSeller",
                identificationText(request.document().identifiesSeller()));
        authenticPeople = replaceFirst(
                authenticPeople,
                ":identifiesBuyer",
                identificationText(request.document().identifiesBuyer()));
        String authenticCar = populateCar(
                templates.carAuthentic(), request.vehicle(), request.seller());
        String authenticTerms = populateDocument(
                templates.document() + templates.secondSectionEnd(),
                request.document(), request.seller(), request.buyer());
        return new CarSaleDocumentSections(
                declarationPeople + declarationCar + declarationTerms,
                legalAgent + authenticPeople + authenticCar + authenticTerms,
                fullName(request.buyer()),
                fullName(request.seller()),
                buyerTitle,
                sellerTitle);
    }

    private String populatePeople(String template, PersonDetails first, String firstTitle,
                                  PersonDetails second, String secondTitle) {
        String populated = populatePerson(template, first, firstTitle);
        return populatePerson(populated, second, secondTitle);
    }

    private String populatePerson(String template, PersonDetails person, String title) {
        String populated = replaceFirst(template, ":givenName", uppercaseName(person.givenName()));
        populated = replaceFirst(populated, ":lastName", uppercaseName(person.lastName()));
        populated = replaceFirst(populated, ":age", person.age());
        populated = replaceFirst(populated, ":job", person.job());
        populated = replaceFirst(populated, ":settlement", person.settlement());
        populated = replaceFirst(populated, ":state", person.state());
        populated = replaceFirst(populated, ":document", person.document());
        return replaceFirst(populated, ":gender", title);
    }

    private String populateCar(String template, CarDetails car, PersonDetails seller) {
        String populated = replaceAll(template, ":sellerOrdinal", ordinal(seller, true));
        populated = replaceAll(populated, ":sellerOwner", gendered(seller, "dueño", "dueña"));
        populated = replaceAll(populated, ":sellerHolder", gendered(seller, "poseedor", "poseedora"));
        populated = replaceFirst(populated, ":licensePlate", car.licensePlate());
        populated = replaceFirst(populated, ":brand", car.brand());
        populated = replaceFirst(populated, ":model", car.model());
        populated = replaceFirst(populated, ":color", car.color());
        populated = replaceFirst(populated, ":factoryYear", car.factoryYear());
        if (isHeavyTruck(car)) {
            populated = replaceFirst(populated, "CAPACIDAD: :capacity; ", "");
        } else {
            populated = replaceFirst(populated, ":capacity", car.capacity());
        }
        populated = replaceFirst(populated, ":domain", car.domain());
        populated = replaceFirst(populated, ":vehicleClass", car.vehicleClass());
        populated = replaceFirst(populated, ":vehicleType", car.vehicleType());
        populated = replaceFirst(populated, ":heavyTruckDetails", heavyTruckDetails(car));
        populated = replaceFirst(populated, ":engineNumber", car.engineNumber());
        populated = replaceFirst(populated, ":chassisNumber", car.chassisNumber());
        return replaceFirst(populated, ":vinNumber", car.vinNumber());
    }

    private String heavyTruckDetails(CarDetails car) {
        if (!isHeavyTruck(car)) {
            return "";
        }
        return "EJES: " + value(car.axles())
                + "; TARA: " + value(car.tare())
                + "; TIPO DE CAPACIDAD: " + value(car.capacityType())
                + "; CAPACIDAD DE CARGA: " + value(car.loadCapacity())
                + "; CAPACIDAD MÁXIMA: " + value(car.maximumCapacity())
                + "; TRACCIÓN: " + value(car.traction()) + "; ";
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private boolean isHeavyTruck(CarDetails car) {
        return "camión pesado".equalsIgnoreCase(car.vehicleClass());
    }

    private String populateDocument(
            String template,
            DocumentDetails details,
            PersonDetails seller,
            PersonDetails buyer) {
        String populated = replaceFirst(template, ":garment", details.garment());
        String institution = details.institution() == null || details.institution().isBlank()
                ? ""
                : "con " + details.institution();
        populated = replaceFirst(populated, ":institution", institution);
        populated = replaceAll(populated, ":sellerOrdinal", ordinal(seller, true));
        populated = replaceAll(populated, ":buyerOrdinal", ordinal(buyer, false));
        populated = replaceAll(populated, ":buyerRole", gendered(buyer, "el comprador", "la compradora"));
        populated = replaceAll(populated, ":buyerReceived", gendered(buyer, "recibido", "recibida"));
        populated = replaceAll(populated, ":sellerRole", gendered(seller, "el vendedor", "la vendedora"));
        populated = replaceAll(populated, ":bothContracting",
                isFemale(seller.gender()) && isFemale(buyer.gender())
                        ? "ambas contratantes"
                        : "ambos contratantes");
        populated = replaceAll(populated, ":appearingParties",
                isFemale(seller.gender()) && isFemale(buyer.gender())
                        ? "las comparecientes"
                        : "los comparecientes");
        populated = replaceFirst(populated, ":price", details.price());
        populated = replaceFirst(populated, ":settlement", details.settlement());
        populated = replaceFirst(populated, ":state", details.state());
        return replaceFirst(populated, ":signDate", details.signDate());
    }

    private String populateLegalAgent(String template, LegalAgentDetails agent, DocumentDetails details) {
        String populated = replaceFirst(template, ":settlement", details.settlement());
        populated = replaceFirst(populated, ":state", details.state());
        populated = replaceFirst(populated, ":signHour", details.signHour());
        populated = replaceFirst(populated, ":signDate", details.signDate());
        populated = replaceFirst(populated, ":givenName", uppercaseName(agent.givenName()));
        populated = replaceFirst(populated, ":lastName", uppercaseName(agent.lastName()));
        populated = replaceFirst(populated, ":gender", legalAgentTitle(agent));
        populated = replaceFirst(populated, ":settlement", agent.settlement());
        return replaceFirst(populated, ":state", agent.state());
    }

    private String legalAgentTitle(LegalAgentDetails agent) {
        boolean female = isFemale(agent.gender());
        if ("abogado".equalsIgnoreCase(agent.role())) {
            return female ? LAWYER_WOMAN : LAWYER_DEFAULT;
        }
        return female ? LEGAL_WOMAN : LEGAL_DEFAULT;
    }

    private String buyerTitle(PersonDetails buyer) {
        return isFemale(buyer.gender()) ? BUYER_WOMAN : BUYER_DEFAULT;
    }

    private String sellerTitle(PersonDetails seller) {
        return isFemale(seller.gender()) ? SELLER_WOMAN : SELLER_DEFAULT;
    }

    private boolean isFemale(String gender) {
        return "femenino".equalsIgnoreCase(gender);
    }

    private String ordinal(PersonDetails person, boolean first) {
        if (first) {
            return gendered(person, "el primero", "la primera");
        }
        return gendered(person, "el segundo", "la segunda");
    }

    private String gendered(PersonDetails person, String masculine, String feminine) {
        return isFemale(person.gender()) ? feminine : masculine;
    }

    private String uppercaseName(String name) {
        return name.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.forLanguageTag("es-SV"));
    }

    private String fullName(PersonDetails person) {
        return uppercaseName(person.givenName()) + " " + uppercaseName(person.lastName());
    }

    private String identificationText(String identified) {
        return "No".equalsIgnoreCase(identified) ? "a quien no conozco" : "a quien hoy conozco";
    }

    private String replaceFirst(String source, String placeholder, String value) {
        int position = source.indexOf(placeholder);
        if (position < 0) {
            return source;
        }
        return source.substring(0, position) + value + source.substring(position + placeholder.length());
    }

    private String replaceAll(String source, String placeholder, String value) {
        return source.replace(placeholder, value);
    }
}
