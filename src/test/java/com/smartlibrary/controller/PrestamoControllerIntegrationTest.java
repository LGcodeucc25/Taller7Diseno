package com.smartlibrary.controller;

import com.smartlibrary.repository.LibroRepository;
import com.smartlibrary.repository.PrestamoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de los escenarios del taller "Registrar préstamo".
 * Cada prueba recrea el contexto (y la base H2 con data.sql) para ser independiente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PrestamoControllerIntegrationTest {

    private static final String URL = "/api/prestamos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    private static String cuerpo(Object estudianteId, Object libroId) {
        return "{\"estudianteId\": " + estudianteId + ", \"libroId\": " + libroId + "}";
    }

    @Test
    @DisplayName("Escenario 1: préstamo de libro disponible -> 201 Created y queda registrado")
    void prestamoDeLibroDisponible() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, 1)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/prestamos/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estudiante").value("Ana Gómez"))
                .andExpect(jsonPath("$.libro").value("Clean Code"))
                .andExpect(jsonPath("$.fechaPrestamo").value(LocalDate.now().toString()));

        // R2: el préstamo queda registrado
        assertThat(prestamoRepository.count()).isEqualTo(antes + 1);
        // R3: el libro deja de estar disponible
        assertThat(libroRepository.findById(1L).orElseThrow().isDisponible()).isFalse();
    }

    @Test
    @DisplayName("Escenario 2: libro ya prestado -> 409 Conflict y no se crea préstamo")
    void libroYaPrestado() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.mensaje").value("El libro no está disponible: 3"));

        // R1 / R4 / R5: no se crea un préstamo nuevo
        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Escenario 3: estudiante inexistente -> 404 Not Found")
    void estudianteInexistente() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(999, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("Estudiante no encontrado: 999"));

        assertThat(prestamoRepository.count()).isEqualTo(antes);
        assertThat(libroRepository.findById(1L).orElseThrow().isDisponible()).isTrue();
    }

    @Test
    @DisplayName("Escenario 4: libro inexistente -> 404 Not Found")
    void libroInexistente() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, 999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("Libro no encontrado: 999"));

        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Escenario 5: consulta de préstamos -> 200 OK con registros")
    void consultaDePrestamos() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].estudiante").value("Carlos Ruiz"))
                .andExpect(jsonPath("$[0].libro").value("Refactoring"))
                .andExpect(jsonPath("$[0].fechaPrestamo").exists());
    }

    @Test
    @DisplayName("Solicitud inválida (estudianteId null) -> 400 Bad Request y no se crea préstamo")
    void solicitudInvalida() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(null, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("estudianteId es obligatorio"));

        // R4: una solicitud inválida no crea préstamo
        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }
}
