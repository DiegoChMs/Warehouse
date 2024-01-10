package com.diegomorales.warehouse.controller;

import com.diegomorales.warehouse.dto.WarehouseDTO;
import com.diegomorales.warehouse.exception.BadRequestException;
import com.diegomorales.warehouse.exception.GenericException;
import com.diegomorales.warehouse.exception.NoContentException;
import com.diegomorales.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/warehouse")
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class WarehouseController {

    private final WarehouseService service;

    @PostMapping
    public ResponseEntity<Object> save( @Valid @RequestBody WarehouseDTO dto) throws GenericException, BadRequestException {
        var response = this.service.save(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findOne(@PathVariable("id") Integer id) throws GenericException, BadRequestException{
        var response = this.service.findOne(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") Integer id,  @Valid @RequestBody WarehouseDTO dto) throws BadRequestException, GenericException{
        var response = this.service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Integer id) throws GenericException, BadRequestException{
        this.service.disable(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public ResponseEntity<Page<WarehouseDTO>> findAll(@RequestParam(value = "search", required = false) String search, Pageable page) throws NoContentException, GenericException{
        var response = this.service.findAll(search, page);
        return ResponseEntity.ok(response);
    }

}
