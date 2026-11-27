package microarch.delivery.core.domain.model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;


@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Order extends Aggregate<UUID> {

  @Embedded
  private Location location;
  @Embedded
  private Volume volume;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  private Order(UUID id, Location location, Volume volume) {
    super(id);
    this.location = location;
    this.volume = volume;
    this.status = OrderStatus.CREATED;
  }

  public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
    var err = Guard.combine(
        Guard.againstNullOrEmpty(id, "id"),
        Guard.againstNullOrEmpty(location, "location"),
        Guard.againstNullOrEmpty(volume, "volume"));
    if (err != null) return Result.failure(err);

    var order = new Order(id, location, volume);
    return Result.success(order);
  }

  public UnitResult<Error> changeStatus(OrderStatus status) {
    var err = Guard.againstNullOrEmpty(status, "status");
    if (err != null) {
      return UnitResult.failure(err);
    }
    var isAllowed = (this.status == OrderStatus.CREATED && status == OrderStatus.ASSIGNED)
        || (this.status == OrderStatus.ASSIGNED && status == OrderStatus.COMPLETED);
    if (!isAllowed) {
      return UnitResult.failure(Error.of("order.status.invalid.transition",
          "Cannot change order status from " + this.status + " to " + status + "."));
    }

    this.status = status;
    return UnitResult.success();
  }
}
