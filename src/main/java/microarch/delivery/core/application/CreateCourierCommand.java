package microarch.delivery.core.application;

/**
 * Команда создания курьера.
 *
 * <p>
 * Менеджер вводит только имя; начальное местоположение назначается случайно.
 *
 * @param name имя курьера; не должно быть пустым
 */
public record CreateCourierCommand(String name) {
}
