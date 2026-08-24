package com.big.dreamer.doccentral.document.carsale.template;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates/car-sale")
public class CarSaleTemplateController {

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("people-document.txt", "Comparecientes del contrato"),
            Map.entry("people-authentic.txt", "Comparecientes de la auténtica"),
            Map.entry("car-document.txt", "Vehículo del contrato"),
            Map.entry("car-authentic.txt", "Vehículo de la auténtica"),
            Map.entry("document.txt", "Condiciones del contrato"),
            Map.entry("document-authentic.txt", "Condiciones de la auténtica"),
            Map.entry("first-section-end.txt", "Cierre del contrato"),
            Map.entry("second-section-end.txt", "Cierre de la auténtica"),
            Map.entry("legal-authentic.txt", "Auténtica legal"));

    private final CarSaleTemplateRepository repository;

    public CarSaleTemplateController(CarSaleTemplateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TemplateView> list() {
        Map<String, String> current = repository.findAll();
        Map<String, String> defaults = repository.defaults();
        List<TemplateView> result = new ArrayList<>();
        current.forEach((name, content) -> result.add(view(name, content, defaults.get(name))));
        return result;
    }

    @PutMapping("/{name}")
    public TemplateView save(@PathVariable String name, @RequestBody TemplateUpdate update) {
        try {
            String content = repository.save(name, update.content());
            return view(name, content, repository.defaults().get(name));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping("/{name}/reset")
    public TemplateView reset(@PathVariable String name) {
        try {
            String content = repository.reset(name);
            return view(name, content, content);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }

    private TemplateView view(String name, String content, String defaultContent) {
        return new TemplateView(name, LABELS.getOrDefault(name, name), content, defaultContent,
                repository.placeholders(defaultContent).stream().toList(), content.equals(defaultContent));
    }

    public record TemplateUpdate(String content) {}
    public record TemplateView(String name, String label, String content, String defaultContent,
                               List<String> requiredVariables, boolean usingDefault) {}
}
