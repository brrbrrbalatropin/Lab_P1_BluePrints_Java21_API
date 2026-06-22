package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.dto.ApiResponse;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejo centralizado de errores para toda la API REST.
 * <p>
 * Convierte cada excepción del dominio o de validación en una respuesta
 * {@link ApiResponse} con el código HTTP correcto, evitando bloques
 * {@code try/catch} repetidos en los controladores.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Recurso inexistente -> 404 Not Found. */
    @ExceptionHandler(BlueprintNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(BlueprintNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.of(404, ex.getMessage(), null));
    }

    /** Conflicto de persistencia (p. ej. blueprint duplicado) -> 409 Conflict. */
    @ExceptionHandler(BlueprintPersistenceException.class)
    public ResponseEntity<ApiResponse<Void>> handlePersistence(BlueprintPersistenceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.of(409, ex.getMessage(), null));
    }

    /** Fallo de validación de Bean Validation (@Valid) -> 400 Bad Request. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) message = "Invalid request payload";
        return ResponseEntity.badRequest()
                .body(ApiResponse.of(400, message, null));
    }

    /** Cuerpo JSON ausente o malformado -> 400 Bad Request. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.of(400, "Malformed or missing request body", null));
    }
}