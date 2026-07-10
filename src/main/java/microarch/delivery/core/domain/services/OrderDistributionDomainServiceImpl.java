package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OrderDistributionDomainServiceImpl implements OrderDistributionDomainService {

    @Override
    public Result<Courier, Error> distributeOrder(Order order, List<Courier> couriers) {
        Error validationError = Guard.combine(
                required(order, "order"),
                required(couriers, "couriers")
        );

        if (validationError != null) {
            return Result.failure(validationError);
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            return Result.failure(GeneralErrors.valueIsInvalid("order.status", order.getStatus()));
        }

        return couriers.stream()
                .filter(Objects::nonNull)
                .filter(courier -> courier.canTakeOneMoreOrder(order.getVolume()))
                .min(Comparator.comparingInt(courier -> courier.getLocation().distanceTo(order.getLocation())))
                .map(courier -> assignOrder(order, courier))
                .orElseGet(() -> Result.failure(Error.of(
                        "order.dispatch.failed",
                        "No available couriers for order"
                )));
    }

    private Result<Courier, Error> assignOrder(Order order, Courier courier) {
        UnitResult<Error> takeOrderResult = courier.takeOrder(order);

        if (takeOrderResult.isFailure()) {
            return Result.failure(takeOrderResult.getError());
        }

        UnitResult<Error> assignOrderResult = order.assign();

        if (assignOrderResult.isFailure()) {
            return Result.failure(assignOrderResult.getError());
        }

        return Result.success(courier);
    }

    private static Error required(Object value, String paramName) {
        return value == null ? GeneralErrors.valueIsRequired(paramName) : null;
    }
}
