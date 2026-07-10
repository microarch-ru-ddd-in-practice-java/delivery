package microarch.delivery.core.application.queries.dto;

import java.util.UUID;

public record IncompleteOrderDto(UUID id, LocationDto location) {
}
