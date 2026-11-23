package microarch.delivery.core.domain.services;

import java.util.Comparator;
import java.util.List;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

public final class DispatchService {

  private static final Error NO_AVAILABLE_COURIER = Error.of("dispatch.courier.not.found",
      "No courier can take this order.");

  public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
    var error = Guard.againstNullOrEmpty(order, "order");
    if (error != null) {
      return Result.failure(error);
    }
    if (order.getStatus() != OrderStatus.CREATED) {
      return Result.failure(Error.of("dispatch.order.not.created",
          "Only an order with Created status can be dispatched."));
    }
    if (Guard.againstNullOrEmpty(couriers, "couriers") != null) {
      return Result.failure(NO_AVAILABLE_COURIER);
    }

    var nearestCourier = couriers.stream()
        .filter(courier -> courier.canTakeOrder(order))
        .min(Comparator.comparingInt(
            courier -> courier.getLocation().distanceTo(order.getLocation())));
    if (nearestCourier.isEmpty()) {
      return Result.failure(NO_AVAILABLE_COURIER);
    }

    var winner = nearestCourier.get();
    var assignmentResult = winner.takeOrder(order);
    if (assignmentResult.isFailure()) {
      return Result.failure(assignmentResult.getError());
    }
    var statusResult = order.changeStatus(OrderStatus.ASSIGNED);
    if (statusResult.isFailure()) {
      return Result.failure(statusResult.getError());
    }

    return Result.success(winner);
  }
}
