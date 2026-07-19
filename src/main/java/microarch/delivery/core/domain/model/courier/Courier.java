package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Агрегат, представляющий курьера.
 *
 * <p>
 * Курьер создаётся с именем и начальным местоположением. Максимальный суммарный объём заказов курьера —
 * {@value DEFAULT_MAX_VOLUME}. Курьер может брать новые заказы, перемещаться на 1 шаг за раз и завершать назначения.
 */
@Entity
@Table(name = "couriers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Courier extends Aggregate<UUID> {

    private static final int DEFAULT_MAX_VOLUME = 20;
    private static final int MAX_STEP_DISTANCE = 1;

    @Column(name = "name")
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "max_volume"))
    private Volume maxVolume;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id")
    private List<Assignment> assignments = new ArrayList<>();

    @Embedded
    private Location location;

    private Courier(UUID id, String name, Location location) {
        super(id);
        this.name = name;
        this.location = location;
        this.maxVolume = Volume.create(DEFAULT_MAX_VOLUME).getValueOrThrow();
        this.assignments = new ArrayList<>();
    }

    /**
     * Создаёт курьера с валидацией имени и местоположения.
     *
     * <p>
     * Id генерируется автоматически. MaxVolume устанавливается равным {@value DEFAULT_MAX_VOLUME}.
     *
     * @param name     имя курьера; обязательно, не должно быть пустым
     * @param location начальное местоположение курьера; обязательно
     * @return {@code Result.success} с курьером, либо {@code Result.failure} если параметр невалиден
     */
    public static Result<Courier, Error> create(String name, Location location) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(name, "name"),
                location == null ? GeneralErrors.valueIsRequired("location") : null);
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Courier(UUID.randomUUID(), name, location));
    }

    /**
     * Возвращает список назначений курьера (только для чтения).
     *
     * @return неизменяемый список назначений
     */
    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    /**
     * Проверяет, может ли курьер взять дополнительный заказ с указанным объёмом.
     *
     * <p>
     * Курьер может взять заказ, если суммарный объём текущих назначений плюс объём нового заказа не превышает
     * {@code maxVolume}.
     *
     * @param orderVolume объём нового заказа; не должен быть {@code null}
     * @return {@code true} если курьер может взять заказ, {@code false} иначе
     * @throws NullPointerException если {@code orderVolume} равен {@code null}
     */
    public boolean canTakeOrder(Volume orderVolume) {
        Objects.requireNonNull(orderVolume, "orderVolume must not be null");
        int currentTotal = assignments.stream().mapToInt(a -> a.getVolume().getValue()).sum();
        return currentTotal + orderVolume.getValue() <= maxVolume.getValue();
    }

    /**
     * Добавляет назначение курьеру, если не превышен максимальный объём.
     *
     * @param orderId       идентификатор заказа; обязателен
     * @param volume        объём заказа; обязателен
     * @param orderLocation местоположение заказа; обязательно
     * @return {@code UnitResult.success} если назначение добавлено, {@code UnitResult.failure} если параметр невалиден
     * или превышен объём
     */
    public UnitResult<Error> addAssignment(UUID orderId, Volume volume, Location orderLocation) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(orderId, "orderId"),
                volume == null ? GeneralErrors.valueIsRequired("volume") : null,
                orderLocation == null ? GeneralErrors.valueIsRequired("orderLocation") : null);
        if (error != null) {
            return UnitResult.failure(error);
        }
        if (!canTakeOrder(volume)) {
            return UnitResult.failure(Errors.maxVolumeExceeded());
        }
        var result = Assignment.create(UUID.randomUUID(), orderId, volume, orderLocation);
        if (result.isFailure()) {
            return UnitResult.failure(result.getError());
        }
        assignments.add(result.getValue());
        return UnitResult.success();
    }

    /**
     * Завершает назначение по идентификатору заказа.
     *
     * <p>
     * Завершение возможно только если курьер находится в одной клетке или ближе от местоположения заказа.
     *
     * @param orderId идентификатор заказа; не должен быть {@code null}
     * @return {@code UnitResult.success} если назначение завершено, {@code UnitResult.failure} если назначение не
     * найдено или курьер слишком далеко
     * @throws NullPointerException если {@code orderId} равен {@code null}
     */
    public UnitResult<Error> completeAssignment(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return assignments.stream().filter(a -> a.getOrderId().equals(orderId)).findFirst()
                .map(a -> a.complete(location))
                .orElse(UnitResult.failure(GeneralErrors.notFound("Assignment", orderId)));
    }

    /**
     * Перемещает курьера в указанное местоположение.
     *
     * <p>
     * Курьер может переместиться не дальше чем на 1 шаг (манхэттенское расстояние ≤ 1).
     *
     * @param newLocation новое местоположение; не должно быть {@code null}
     * @return {@code UnitResult.success} если перемещение выполнено, {@code UnitResult.failure} если расстояние до
     * нового местоположения больше 1
     * @throws NullPointerException если {@code newLocation} равен {@code null}
     */
    public UnitResult<Error> move(Location newLocation) {
        Objects.requireNonNull(newLocation, "newLocation must not be null");
        if (MAX_STEP_DISTANCE < location.distanceTo(newLocation)) {
            return UnitResult.failure(Errors.tooFarToMove());
        }
        location = newLocation;
        return UnitResult.success();
    }

    public static class Errors {
        public static Error maxVolumeExceeded() {
            return Error.of("courier.max.volume.exceeded",
                    "Courier cannot take more orders: max volume would be exceeded");
        }

        public static Error tooFarToMove() {
            return Error.of("courier.move.too.far", "Courier can only move 1 step at a time");
        }
    }
}
