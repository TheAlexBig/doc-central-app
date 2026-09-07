package com.big.dreamer.doccentral.document.mutual.template;

import com.big.dreamer.doccentral.document.template.EditableTemplateRepository.TemplateView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/templates/mutual")
public class MutualTemplateController {
    private final MutualTemplateRepository repository;

    public MutualTemplateController(MutualTemplateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TemplateView> list() {
        return repository.list();
    }

    @PutMapping("/{name}")
    public TemplateView save(@PathVariable String name, @RequestBody TemplateUpdate update) {
        try {
            return repository.save(name, update.content());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping("/{name}/reset")
    public TemplateView reset(@PathVariable String name) {
        try {
            return repository.reset(name);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }

    public record TemplateUpdate(String content) {}
}
