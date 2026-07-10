package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Courier extends Aggregate<UUID> {

    public static final int MAX_VOLUME = 20;

    @Getter
    private String name;
    @Getter
    private Location location;
    @Getter
    private Volume maxVolume;
    private final List<Assignment> assignments = new ArrayList<>();

    protected Courier() {
    }

    private Courier(UUID id, String name, Location location) {
        super(id);
        this.name = name;
        this.location = location;
        this.maxVolume = Volume.mustCreate(MAX_VOLUME);
    }

    private Courier(UUID id, String name, Location location, Volume maxVolume, List<Assignment> assignments) {
        super(id);
        this.name = name;
        this.location = location;
        this.maxVolume = maxVolume;
        this.assignments.addAll(assignments);
    }

    public static Result<Courier, Error> create(String name, Location location) {
        return create(UUID.randomUUID(), name, location);
    }

    public static Result<Courier, Error> create(UUID id, String name, Location location) {
        Error validationError = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                Guard.againstNullOrEmpty(name, "name"), required(location, "location"));

        if (validationError != null) {
            return Result.failure(validationError);
        }

        return Result.success(new Courier(id, name, location));
    }

    public static Result<Courier, Error> restore(
            UUID id,
            String name,
            Location location,
            Volume maxVolume,
            List<Assignment> assignments
    ) {
        Error validationError = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                Guard.againstNullOrEmpty(name, "name"), required(location, "location"),
                required(maxVolume, "maxVolume"), required(assignments, "assignments"));

        if (validationError != null) {
            return Result.failure(validationError);
        }

        if (assignments.stream().anyMatch(Objects::isNull)) {
            return Result.failure(GeneralErrors.valueIsInvalid("assignments", assignments));
        }

        return Result.success(new Courier(id, name, location, maxVolume, assignments));
    }

    public boolean canTakeOneMoreOrder(Volume volume) {
        if (volume == null) {
            return false;
        }

        return currentAssignedVolume() + volume.getValue() <= maxVolume.getValue();
    }

    public UnitResult<Error> takeOrder(Order order) {
        Error validationError = required(order, "order");

        if (validationError != null) {
            return UnitResult.failure(validationError);
        }

        if (!canTakeOneMoreOrder(order.getVolume())) {
            return UnitResult.failure(GeneralErrors.valueIsInvalid("volume", order.getVolume()));
        }

        Result<Assignment, Error> assignmentResult = Assignment.create(
                order.getId(),
                order.getVolume(),
                order.getLocation()
        );

        if (assignmentResult.isFailure()) {
            return UnitResult.failure(assignmentResult.getError());
        }

        assignments.add(assignmentResult.getValue());
        return UnitResult.success();
    }

    public UnitResult<Error> completeAssignment(UUID assignmentId) {
        Optional<Assignment> assignment = assignments.stream()
                .filter(item -> item.getId().equals(assignmentId))
                .findFirst();

        if (assignment.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("assignment", assignmentId));
        }

        return assignment.get().complete(location);
    }

    public UnitResult<Error> completeAssignmentForOrder(UUID orderId) {
        Error validationError = Guard.againstNullOrEmpty(orderId, "orderId");

        if (validationError != null) {
            return UnitResult.failure(validationError);
        }

        Optional<Assignment> assignment = assignments.stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .findFirst();

        if (assignment.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("assignment", orderId));
        }

        return assignment.get().complete(location);
    }

    public UnitResult<Error> changeLocation(Location newLocation) {
        Error validationError = required(newLocation, "newLocation");

        if (validationError != null) {
            return UnitResult.failure(validationError);
        }

        location = newLocation;
        return UnitResult.success();
    }

    private int currentAssignedVolume() {
        return assignments.stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED)
                .mapToInt(assignment -> assignment.getVolume().getValue())
                .sum();
    }

    private static Error required(Object value, String paramName) {
        return value == null ? GeneralErrors.valueIsRequired(paramName) : null;
    }

    public List<Assignment> getAssignments() {
        return List.copyOf(assignments);
    }
}
