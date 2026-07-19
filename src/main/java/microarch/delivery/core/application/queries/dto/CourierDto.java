package microarch.delivery.core.application.queries.dto;

import microarch.delivery.core.domain.model.kernel.Location;

import java.util.UUID;

/**
 * DTO курьера для отображения на карте.
 *
 * @param id       идентификатор курьера
 * @param name     имя курьера
 * @param location текущее местоположение
 */
public record CourierDto(UUID id, String name, Location location) {
}
