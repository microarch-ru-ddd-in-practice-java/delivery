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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import microarch.delivery.core.domain.model.order.Order;

@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity
@Table(name = "couriers")
public final class Courier extends Aggregate<UUID> {
  private static final int MAX_VOLUME = 20;

  @Getter
  @Column(nullable = false)
  private String name;
  @Getter
  @Embedded
  private Location location;
  @Getter
  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "max_volume", nullable = false))
  private Volume maxVolume;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "courier_id", nullable = false)
  private List<Assignment> assignments;

  private Courier(String name, Location location) {
    super(UUID.randomUUID());
    this.name = name;
    this.location = location;
    this.maxVolume = Volume.create(MAX_VOLUME).getValue();
    this.assignments = new ArrayList<>();
  }

  public static Result<Courier, Error> create(String name, Location location) {
    var error = Guard.combine(
        Guard.againstNullOrEmpty(name, "name"),
        Guard.againstNullOrEmpty(location, "location"));
    if (error != null) {
      return Result.failure(error);
    }
    return Result.success(new Courier(name, location));
  }

  public boolean canTakeOrder(Order order) {
    if (Guard.againstNullOrEmpty(order, "order") != null) {
      return false;
    }
    var orderVolume = order.getVolume();
    if (orderVolume.isGreaterThan(maxVolume)) {
      return false;
    }
    var totalVolume = assignments.stream()
        .filter(assignment -> assignment.getStatus() == Status.ASSIGNED)
        .map(Assignment::getVolume)
        .reduce(orderVolume, Volume::add);
    return !totalVolume.isGreaterThan(maxVolume);
  }

  public UnitResult<Error> takeOrder(Order order) {
    var error = Guard.againstNullOrEmpty(order, "order");
    if (error != null) {
      return UnitResult.failure(error);
    }
    if (assignments.stream().anyMatch(assignment -> assignment.getOrderId().equals(order.getId()))) {
      return UnitResult.failure(Error.of("courier.order.already.assigned",
          "The courier has already taken this order."));
    }
    if (!canTakeOrder(order)) {
      return UnitResult.failure(Error.of("courier.max.volume.exceeded",
          "The total assigned volume must not exceed the courier's maximum volume."));
    }

    var assignment = Assignment.create(order.getId(), order.getVolume(), order.getLocation())
        .getValue();
    assignments.add(assignment);
    return UnitResult.success();
  }

  public UnitResult<Error> completeAssignment(UUID assignmentId) {
    var error = Guard.againstNullOrEmpty(assignmentId, "assignmentId");
    if (error != null) {
      return UnitResult.failure(error);
    }
    var assignment = assignments.stream()
        .filter(current -> current.getId().equals(assignmentId))
        .findFirst();
    if (assignment.isEmpty()) {
      return UnitResult.failure(GeneralErrors.notFound("assignment", assignmentId));
    }

    return assignment.get().complete(location);
  }

  public UnitResult<Error> moveTo(Location location) {
    var error = Guard.againstNullOrEmpty(location, "location");
    if (error != null) {
      return UnitResult.failure(error);
    }
    if (!this.location.isWithinOneStepOf(location)) {
      return UnitResult.failure(Error.of("courier.location.too.far",
          "The courier can move at most one step from the current location."));
    }

    this.location = location;
    return UnitResult.success();
  }
}
