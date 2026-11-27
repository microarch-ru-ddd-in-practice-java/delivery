package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

@Entity
@Table(name = "assignments")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Assignment extends BaseEntity<UUID> {

  @Column(name = "order_id", nullable = false)
  @Getter
  private final UUID orderId;

  @Embedded
  @Getter
  private final Volume volume;

  @Embedded
  @Getter
  private final Location location;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Getter
  private Status status;

  private Assignment(UUID orderId, Volume volume, Location location) {
    super(UUID.randomUUID());
    this.orderId = orderId;
    this.volume = volume;
    this.location = location;
    this.status = Status.ASSIGNED;
  }

  public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(volume, "volume");
    Objects.requireNonNull(location, "location");

    var assignment = new Assignment(orderId, volume, location);
    return Result.success(assignment);
  }

  public UnitResult<Error> complete(Location courierLocation) {
    if (courierLocation == null) {
      return UnitResult.failure(GeneralErrors.valueIsRequired("courierLocation"));
    }
    if (this.status == Status.COMPLETED) {
      return UnitResult.failure(Error.of("assignment.completed",
          "The assignment has already been completed."));
    }
    if (!this.location.isWithinOneStepOf(courierLocation)) {
      return UnitResult.failure(Error.of("assignment.courier.faraway",
          "The courier must be at most one step away from the assignment location."));
    }

    this.status = Status.COMPLETED;
    return UnitResult.success();
  }
}
