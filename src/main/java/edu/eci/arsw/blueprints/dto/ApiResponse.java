package edu.eci.arsw.blueprints.dto;

/**
 * Envoltura uniforme para todas las respuestas de la API.
 * <p>
 * Estandariza el cuerpo JSON en la forma {@code {code, message, data}}, de modo que
 * tanto las respuestas exitosas como los errores comparten el mismo contrato.
 *
 * @param <T> tipo del payload transportado en {@code data}
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "execute ok", data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "created", data);
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(202, "accepted", data);
    }

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}