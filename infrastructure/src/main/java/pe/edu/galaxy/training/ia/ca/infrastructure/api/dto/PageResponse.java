package pe.edu.galaxy.training.ia.ca.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Página de resultados")
public record PageResponse<T>(
        List<T> content,
        @Schema(description = "Página actual, desde 0") int page,
        int size,
        long totalElements,
        int totalPages) {
}
