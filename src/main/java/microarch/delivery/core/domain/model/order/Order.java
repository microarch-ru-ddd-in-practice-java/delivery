package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

public class Order extends Aggregate<UUID> {

    @Getter
    private Volume volume;
    @Getter
    private Location location;
    @Getter
    private OrderStatus status;

    protected Order() {
    }

    private Order(UUID id, Volume volume, Location location) {
        super(id);
        this.volume = volume;
        this.location = location;
        this.status = OrderStatus.CREATED;
    }

    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        Error validationError = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                required(location, "location"), required(volume, "volume"));

        if (validationError != null) {
            return Result.failure(validationError);
        }

        return Result.success(new Order(id, volume, location));
    }

    public static Result<Order, Error> restore(UUID id, Location location, Volume volume, OrderStatus status) {
        Error validationError = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                required(location, "location"), required(volume, "volume"),
                required(status, "status"));

        if (validationError != null) {
            return Result.failure(validationError);
        }

        Order order = new Order(id, volume, location);
        order.status = status;
        return Result.success(order);
    }

    public UnitResult<Error> assign() {
        if (status != OrderStatus.CREATED) {
            return UnitResult.failure(GeneralErrors.valueIsInvalid("status", status));
        }

        status = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (status != OrderStatus.ASSIGNED) {
            return UnitResult.failure(GeneralErrors.valueIsInvalid("status", status));
        }

        status = OrderStatus.COMPLETED;
        return UnitResult.success();
    }

    private static Error required(Object value, String paramName) {
        return value == null ? GeneralErrors.valueIsRequired(paramName) : null;
    }
}
