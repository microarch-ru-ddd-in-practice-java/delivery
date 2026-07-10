package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;

import java.util.UUID;

public record CompleteOrderCommand(UUID courierId, UUID orderId) {

    public Error validate() {
        return Guard.combine(
                Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(orderId, "orderId")
        );
    }
}
