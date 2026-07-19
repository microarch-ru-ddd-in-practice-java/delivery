package microarch.delivery.core.domain.model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

/**
 * Агрегат, представляющий заказ на доставку.
 *
 * <p>
 * Заказ создаётся на основе Id корзины (Id передаётся извне, не генерируется). Жизненный цикл:
 * {@link OrderStatus#CREATED} → {@link OrderStatus#ASSIGNED} → {@link OrderStatus#COMPLETED}. Переход в следующий
 * статус возможен только из предыдущего.
 */
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends Aggregate<UUID> {

    @Embedded
    private Location location;

    @Embedded
    private Volume volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    private Order(UUID id, Location location, Volume volume) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = OrderStatus.CREATED;
    }

    /**
     * Создаёт заказ с валидацией всех обязательных параметров.
     *
     * <p>
     * Заказ создаётся в статусе {@link OrderStatus#CREATED}. Id передаётся извне — в качестве основы используется Id
     * корзины.
     *
     * @param id       идентификатор заказа (Id корзины); обязателен
     * @param location местоположение доставки; обязательно
     * @param volume   объём заказа; обязателен
     * @return {@code Result.success} с заказом, либо {@code Result.failure} если параметр невалиден
     */
    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                location == null ? GeneralErrors.valueIsRequired("location") : null,
                volume == null ? GeneralErrors.valueIsRequired("volume") : null);
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Order(id, location, volume));
    }

    /**
     * Переводит заказ в статус {@link OrderStatus#ASSIGNED}.
     *
     * @return {@code UnitResult.success} если переход выполнен, {@code UnitResult.failure} если текущий статус не
     * {@link OrderStatus#CREATED}
     */
    public UnitResult<Error> assign() {
        if (OrderStatus.CREATED != status) {
            return UnitResult.failure(Errors.cannotAssign(status));
        }
        status = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    /**
     * Переводит заказ в статус {@link OrderStatus#COMPLETED}.
     *
     * @return {@code UnitResult.success} если переход выполнен, {@code UnitResult.failure} если текущий статус не
     * {@link OrderStatus#ASSIGNED}
     */
    public UnitResult<Error> complete() {
        if (OrderStatus.ASSIGNED != status) {
            return UnitResult.failure(Errors.cannotComplete(status));
        }
        status = OrderStatus.COMPLETED;
        return UnitResult.success();
    }

    public static class Errors {

        public static Error cannotAssign(OrderStatus currentStatus) {
            return Error.of("order.cannot.assign", String
                    .format("Order can only be assigned when in Created status. Current status is %s", currentStatus));
        }

        public static Error cannotComplete(OrderStatus currentStatus) {
            return Error.of("order.cannot.complete", String.format(
                    "Order can only be completed when in Assigned status. Current status is %s", currentStatus));
        }
    }
}
