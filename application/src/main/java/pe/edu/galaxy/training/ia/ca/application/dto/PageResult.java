package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.List;
import java.util.function.Function;

/**
 * Página de resultados; {@code application} no conoce {@code org.springframework.data.domain.Page}.
 *
 * @param <T> tipo de los elementos
 * @param content elementos de la página, en una lista inmutable
 * @param page número de página, desde 0
 * @param size tamaño de página pedido
 * @param totalElements total de coincidencias en todas las páginas
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    /** Copia el contenido para que la página no cambie. */
    public PageResult {
        content = List.copyOf(content);
    }

    /** {@return la cantidad de páginas; 0 si no hay coincidencias o el tamaño es 0} */
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    /**
     * Convierte los elementos conservando la paginación, por ejemplo de agregado a resultado.
     * @param <R> tipo de los elementos convertidos
     * @param mapper conversión de cada elemento
     * @return una página nueva con los elementos convertidos
     */
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResult<>(content.stream().<R>map(mapper).toList(), page, size, totalElements);
    }
}
