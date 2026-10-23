package com.ngo.ngoplatform.controller;

import com.ngo.ngoplatform.entity.Ngo;
import com.ngo.ngoplatform.service.NgoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ngos")
@RequiredArgsConstructor
public class NgoController {

    private final NgoService ngoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ngo createNgo(@Valid @RequestBody Ngo ngo) {
        return ngoService.saveNgo(ngo);
    }

    @GetMapping
    public List<Ngo> getAllNgos() {
        return ngoService.getAllNgos();
    }

    @GetMapping("/{id}")
    public Ngo getNgoById(@PathVariable Long id) {
        return ngoService.getNgoById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));
    }

    @PutMapping("/{id}")
    public Ngo updateNgo(@PathVariable Long id, @Valid @RequestBody Ngo ngo) {
        return ngoService.updateNgo(id, ngo);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteNgo(@PathVariable Long id) {
        ngoService.deleteNgo(id);
        return Map.of("message", "NGO account deleted successfully");
    }
}
