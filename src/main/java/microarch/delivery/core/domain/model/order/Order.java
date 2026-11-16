package microarch.delivery.core.domain.model.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import libs.ddd.BaseEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Order extends BaseEntity<UUID> {

}
