package com.big.dreamer.doccentral.person.api;

import com.big.dreamer.doccentral.person.model.SavedPerson;
import com.big.dreamer.doccentral.person.service.SavedPersonRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/people")
public class SavedPersonController {

    private final SavedPersonRepository repository;

    public SavedPersonController(SavedPersonRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SavedPerson> listPeople() {
        return repository.findAll();
    }

    @GetMapping("/occupations")
    public List<String> listOccupations() {
        return repository.findOccupations();
    }

    @PostMapping
    public ResponseEntity<SavedPerson> savePerson(@Valid @RequestBody SavedPerson person) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(person));
    }

    @DeleteMapping("/{documento}")
    public ResponseEntity<Void> deletePerson(@PathVariable String documento) {
        if (!repository.delete(documento)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found.");
        }
        return ResponseEntity.noContent().build();
    }
}
