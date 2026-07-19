package microarch.delivery.core.domain.model.assignment;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.BaseEntity;
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

import java.util.Objects;
import java.util.UUID;

/**
 * Entity, представляющий назначение заказа на курьера.
 *
 * <p>
 * Assignment хранит частичную информацию о заказе: идентификатор, объём и местоположение. Всегда создаётся в статусе
 * {@link AssignmentStatus#ASSIGNED}. Завершить назначение можно только если курьер находится в одной клетке или ближе
 * от целевого местоположения заказа (манхэттенское расстояние ≤ 1).
 *
 * <p>
 * Два Assignment равны, если равны их идентификаторы.
 */
@Entity
@Table(name = "assignments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Assignment extends BaseEntity<UUID> {

    private static final int COMPLETION_DISTANCE = 1;

    @Column(name = "order_id")
    private UUID orderId;

    @Embedded
    private Volume volume;

    @Embedded
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssignmentStatus status;

    private Assignment(UUID id, UUID orderId, Volume volume, Location location) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = AssignmentStatus.ASSIGNED;
    }

    /**
     * Создаёт назначение с валидацией всех обязательных параметров.
     *
     * <p>
     * Назначение создаётся в статусе {@link AssignmentStatus#ASSIGNED}.
     *
     * @param id       уникальный идентификатор назначения; обязателен
     * @param orderId  уникальный идентификатор заказа; обязателен
     * @param volume   объём заказа; обязателен
     * @param location местоположение заказа; обязательно
     * @return {@code Result.success} с Assignment, либо {@code Result.failure} если параметр невалиден
     */
    public static Result<Assignment, Error> create(UUID id, UUID orderId, Volume volume, Location location) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(id, "id"), Guard.againstNullOrEmpty(orderId, "orderId"),
                volume == null ? GeneralErrors.valueIsRequired("volume") : null,
                location == null ? GeneralErrors.valueIsRequired("location") : null);

        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Assignment(id, orderId, volume, location));
    }

    /**
     * Завершает назначение, если курьер находится достаточно близко к целевому местоположению.
     *
     * <p>
     * Завершение возможно только если манхэттенское расстояние от курьера до местоположения заказа не превышает 1 (одна
     * клетка или та же клетка).
     *
     * @param courierLocation текущее местоположение курьера; не должно быть {@code null}
     * @return {@code UnitResult.success} если назначение успешно завершено, {@code UnitResult.failure} если курьер
     * слишком далеко или назначение уже завершено
     * @throws NullPointerException если {@code courierLocation} равен {@code null}
     */
    public UnitResult<Error> complete(Location courierLocation) {
        Objects.requireNonNull(courierLocation, "courierLocation must not be null");
        if (AssignmentStatus.COMPLETED == status) {
            return UnitResult.failure(Errors.alreadyCompleted());
        }
        if (COMPLETION_DISTANCE < courierLocation.distanceTo(location)) {
            return UnitResult.failure(Errors.courierTooFar());
        }
        status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }

    public static class Errors {
        public static Error alreadyCompleted() {
            return Error.of("assignment.already.completed", "Assignment is already completed");
        }

        public static Error courierTooFar() {
            return Error.of("assignment.courier.too.far",
                    "Courier is too far from the assignment location to complete it");
        }
    }
}
