package com.smartlibrary.service;

import com.smartlibrary.dto.PrestamoRequest;
import com.smartlibrary.exception.LibroNoDisponibleException;
import com.smartlibrary.exception.RecursoNoEncontradoException;
import com.smartlibrary.model.Estudiante;
import com.smartlibrary.model.Libro;
import com.smartlibrary.model.Prestamo;
import com.smartlibrary.repository.EstudianteRepository;
import com.smartlibrary.repository.LibroRepository;
import com.smartlibrary.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private final EstudianteRepository estudianteRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public PrestamoService(EstudianteRepository estudianteRepository,
                           LibroRepository libroRepository,
                           PrestamoRepository prestamoRepository) {
        this.estudianteRepository = estudianteRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    /**
     * Registra un préstamo siguiendo el diagrama de secuencia.
     * Si alguna validación falla se lanza una excepción y la transacción
     * no crea ningún préstamo (R4).
     */
    @Transactional
    public Prestamo registrarPrestamo(PrestamoRequest request) {
        // 1. Buscar estudiante
        Estudiante estudiante = estudianteRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estudiante no encontrado: " + request.getEstudianteId()));

        // 2. Buscar libro
        Libro libro = libroRepository.findById(request.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Libro no encontrado: " + request.getLibroId()));

        // 3. R1 / R5: solo un libro disponible (sin préstamo activo) puede prestarse
        if (!libro.isDisponible()) {
            throw new LibroNoDisponibleException(
                    "El libro no está disponible: " + request.getLibroId());
        }

        // 4. R2: el préstamo registra estudiante, libro y fecha
        Prestamo prestamo = new Prestamo(estudiante, libro, LocalDate.now());

        // 5. R3: el libro deja de estar disponible (Experto GRASP)
        libro.marcarPrestado();

        // 6. Persistir el cambio del libro
        libroRepository.save(libro);

        // 7. Persistir y devolver el préstamo
        return prestamoRepository.save(prestamo);
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listarPrestamos() {
        return prestamoRepository.findAll();
    }
}
