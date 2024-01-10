package com.diegomorales.warehouse.controller;

import com.diegomorales.warehouse.dto.UserDTO;
import com.diegomorales.warehouse.exception.BadRequestException;
import com.diegomorales.warehouse.exception.GenericException;
import com.diegomorales.warehouse.exception.NoContentException;
import com.diegomorales.warehouse.service.UserService;
import com.diegomorales.warehouse.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/user")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService service;

    private final TokenUtils tokenUtils;

    @PostMapping
    public ResponseEntity<Object> save(@Valid @RequestBody UserDTO dto) throws GenericException, BadRequestException, ResponseStatusException {
        var response = this.service.save(dto);
        return ResponseEntity.created(URI.create("/api/user/" + response.getId())).build();
    }

    @PostMapping("/register")
    public ResponseEntity<Object> saveWithDefaultRole(@Valid @RequestBody UserDTO dto) throws GenericException, BadRequestException {
        var response = this.service.saveWithDefaultRole(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findOne(@PathVariable("id") Integer id) throws GenericException, BadRequestException {
        var response = this.service.findOne(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Integer id, @Valid @RequestBody UserDTO dto) throws GenericException, BadRequestException {
        this.service.update(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Integer id) throws GenericException, BadRequestException {
        this.service.disable(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(@RequestParam(value = "search", required = false) String search, Pageable page) throws GenericException, NoContentException {
        var response = this.service.findAll(search, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<Object> profile(HttpServletRequest httpServletRequest) throws GenericException, BadRequestException{
        var response = this.tokenUtils.getUserToken(httpServletRequest);
        return ResponseEntity.ok(response);
    }

}
