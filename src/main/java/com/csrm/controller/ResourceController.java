package com.csrm.controller;

import com.csrm.model.Resource;
import com.csrm.security.AuthUser;
import com.csrm.service.ResourceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    private final ResourceService service;
    public ResourceController(ResourceService service) { this.service = service; }

    @GetMapping
    public List<Resource> list() { return service.list(); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Resource create(@Valid @RequestBody Resource r, @AuthenticationPrincipal AuthUser me) { return service.create(r, me); }

    @PutMapping("/{id}")
    public Resource update(@PathVariable Long id, @Valid @RequestBody Resource r, @AuthenticationPrincipal AuthUser me) {
        return service.update(id, r, me);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthUser me) { service.delete(id, me); }
}
