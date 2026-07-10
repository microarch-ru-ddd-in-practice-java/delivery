package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import microarch.delivery.core.domain.model.kernel.Location;

import java.util.UUID;

public record MoveCourierCommand(UUID courierId, int locationX, int locationY) {

    public Error validate() {
        return Guard.combine(
                Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstOutOfRange(locationX, Location.MIN_COORDINATE,
                        Location.MAX_COORDINATE, "locationX"),
                Guard.againstOutOfRange(locationY, Location.MIN_COORDINATE,
                        Location.MAX_COORDINATE, "locationY")
        );
    }
}
