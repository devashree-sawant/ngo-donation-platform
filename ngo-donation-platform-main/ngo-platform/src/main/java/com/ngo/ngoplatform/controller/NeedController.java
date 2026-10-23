package com.ngo.ngoplatform.controller;

import com.ngo.ngoplatform.entity.Need;
import com.ngo.ngoplatform.service.NeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/needs")
@RequiredArgsConstructor
public class NeedController {

    private final NeedService needService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Need createNeed(@Valid @RequestBody Need need) {
        return needService.saveNeed(need);
    }

    @GetMapping
    public List<Need> getAllNeeds() {
        return needService.getAllNeeds();
    }

    @GetMapping("/{id}")
    public Need getNeedById(@PathVariable Long id) {
        return needService.getNeedById(id);
    }

    @PutMapping("/{id}")
    public Need updateNeed(@PathVariable Long id, @Valid @RequestBody Need need) {
        return needService.updateNeed(id, need);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteNeed(@PathVariable Long id) {
        needService.deleteNeed(id);
        return Map.of("message", "Campaign deleted successfully");
    }

    @GetMapping("/{id}/progress")
    public Map<String, Object> getNeedProgress(@PathVariable Long id) {
        return needService.getNeedProgress(id);
    }
}
