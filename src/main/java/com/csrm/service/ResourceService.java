package com.csrm.service;

import com.csrm.exception.ApiException;
import com.csrm.model.Resource;
import com.csrm.repository.ResourceRepository;
import com.csrm.security.AuthUser;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {
    private final ResourceRepository repo;
    private final AuditService audit;
    public ResourceService(ResourceRepository repo, AuditService audit) { this.repo = repo; this.audit = audit; }

    public List<Resource> list() { return repo.findAll(); }

    public Resource create(Resource r, AuthUser me) {
        r.id = null;
        Resource saved = repo.save(r);
        audit.log(me.id(), me.username(), "RESOURCE_ADDED " + saved.name);
        return saved;
    }

    public Resource update(Long id, Resource in, AuthUser me) {
        Resource r = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resource not found."));
        r.name = in.name; r.type = in.type; r.location = in.location; r.availability = in.availability;
        audit.log(me.id(), me.username(), "RESOURCE_UPDATED " + r.name);
        return repo.save(r);
    }

    public void delete(Long id, AuthUser me) {
        Resource r = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resource not found."));
        repo.delete(r);
        repo.flush();
        audit.log(me.id(), me.username(), "RESOURCE_REMOVED " + r.name);
    }
}
