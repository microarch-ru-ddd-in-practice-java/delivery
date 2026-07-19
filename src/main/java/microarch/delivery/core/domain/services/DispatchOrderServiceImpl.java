package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Реализация доменного сервиса распределения заказов.
 *
 * <p>
 * Алгоритм: из списка курьеров исключаются переполненные, среди оставшихся выбирается ближайший к местоположению заказа
 * (по манхэттенскому расстоянию).
 */
@Service
public class DispatchOrderServiceImpl implements DispatchOrderService {

    @Override
    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(couriers, "couriers must not be null");

        return findClosestAvailable(order, couriers).map(courier -> assignOrder(courier, order))
                .orElseGet(() -> Result.failure(DispatchOrderService.Errors.noCourierAvailable()));
    }

    private Optional<Courier> findClosestAvailable(Order order, List<Courier> couriers) {
        return couriers.stream().filter(c -> c.canTakeOrder(order.getVolume()))
                .min(Comparator.comparingInt(c -> c.getLocation().distanceTo(order.getLocation())));
    }

    private Result<Courier, Error> assignOrder(Courier courier, Order order) {
        courier.addAssignment(order.getId(), order.getVolume(), order.getLocation()).getOrElseThrow();
        order.assign().getOrElseThrow();
        return Result.success(courier);
    }
}
