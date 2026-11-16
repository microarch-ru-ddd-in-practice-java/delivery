package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import libs.ddd.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

@Entity
@Table(name = "assignment")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Assignment extends BaseEntity<UUID> {
  @Column(name = "order_id")
  @Getter
  private final UUID orderId;

  @Column(name = "volume")
  @Getter
  private final Volume volume;

  @Column(name = "location")
  @Getter
  private final Location location;

  @Column(name = "status")
  @Getter
  private final Status status;
}
