package microarch.delivery.core.domain.model.courier;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

@Getter
public class Assignment extends BaseEntity<UUID> {
    private static final int MAX_DISTANCE_TO_COMPLETE = 1;

    private UUID orderId;
    @Getter
    private Volume volume;
    private Location location;
    private AssignmentStatus status;

    protected Assignment() {
    }

    private Assignment(UUID id, UUID orderId, Volume volume, Location location) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = AssignmentStatus.ASSIGNED;
    }

    public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {
        return create(UUID.randomUUID(), orderId, volume, location);
    }

    public static Result<Assignment, Error> create(
            UUID id,
            UUID orderId,
            Volume volume,
            Location location
    ) {
        Error validationError = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                Guard.againstNullOrEmpty(orderId, "orderId"), required(volume, "volume"),
                required(location, "location"));

        if (validationError != null) {
            return Result.failure(validationError);
        }

        return Result.success(new Assignment(id, orderId, volume, location));
    }

    public static Result<Assignment, Error> restore(
            UUID id,
            UUID orderId,
            Volume volume,
            Location location,
            AssignmentStatus status
    ) {
        Error validationError = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                Guard.againstNullOrEmpty(orderId, "orderId"), required(volume, "volume"),
                required(location, "location"), required(status, "status"));

        if (validationError != null) {
            return Result.failure(validationError);
        }

        Assignment assignment = new Assignment(id, orderId, volume, location);
        assignment.status = status;
        return Result.success(assignment);
    }

    public UnitResult<Error> complete(Location courierLocation) {
        Error validationError = required(courierLocation, "courierLocation");

        if (validationError != null) {
            return UnitResult.failure(validationError);
        }

        if (status != AssignmentStatus.ASSIGNED) {
            return UnitResult.failure(GeneralErrors.valueIsInvalid("status", status));
        }

        if (location.distanceTo(courierLocation) > MAX_DISTANCE_TO_COMPLETE) {
            return UnitResult.failure(
                    GeneralErrors.valueIsInvalid("courierLocation", courierLocation)
            );
        }

        status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }

    private static Error required(Object value, String paramName) {
        return value == null ? GeneralErrors.valueIsRequired(paramName) : null;
    }
}
