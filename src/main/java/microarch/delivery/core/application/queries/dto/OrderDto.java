package microarch.delivery.core.application.queries.dto;

import microarch.delivery.core.domain.model.kernel.Location;

import java.util.UUID;

/**
 * DTO незавершённого заказа для отображения на карте.
 *
 * @param id
 *            идентификатор заказа
 * @param location
 *            местоположение доставки
 */
public record OrderDto(UUID id, Location location) {
}
