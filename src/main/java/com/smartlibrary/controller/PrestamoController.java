package com.smartlibrary.controller;

import com.smartlibrary.dto.PrestamoRequest;
import com.smartlibrary.dto.PrestamoResponse;
import com.smartlibrary.model.Prestamo;
import com.smartlibrary.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> registrar(@Valid @RequestBody PrestamoRequest request) {
        Prestamo prestamo = prestamoService.registrarPrestamo(request);
        URI location = URI.create("/api/prestamos/" + prestamo.getId());
        return ResponseEntity.created(location).body(PrestamoResponse.desde(prestamo));
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponse>> listar() {
        List<PrestamoResponse> prestamos = prestamoService.listarPrestamos().stream()
                .map(PrestamoResponse::desde)
                .toList();
        return ResponseEntity.ok(prestamos);
    }
}
