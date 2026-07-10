package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;

import java.util.UUID;

public record CreateOrderCommand(
        UUID orderId,
        String country,
        String city,
        String street,
        String house,
        String apartment,
        int volume
) {

    public Error validate() {
        return Guard.combine(
                Guard.againstNullOrEmpty(orderId, "orderId"),
                Guard.againstNullOrEmpty(country, "country"),
                Guard.againstNullOrEmpty(city, "city"),
                Guard.againstNullOrEmpty(street, "street"),
                Guard.againstNullOrEmpty(house, "house"),
                Guard.againstNullOrEmpty(apartment, "apartment")
        );
    }
}
