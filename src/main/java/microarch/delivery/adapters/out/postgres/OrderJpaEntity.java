package microarch.delivery.adapters.out.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Column(name = "volume", nullable = false)
    private int volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    protected OrderJpaEntity() {
    }

    private OrderJpaEntity(UUID id, int locationX, int locationY, int volume, OrderStatus status) {
        this.id = id;
        this.locationX = locationX;
        this.locationY = locationY;
        this.volume = volume;
        this.status = status;
    }

    static OrderJpaEntity fromDomain(Order order) {
        Objects.requireNonNull(order, "order must not be null");

        return new OrderJpaEntity(
                order.getId(),
                order.getLocation().getX(),
                order.getLocation().getY(),
                order.getVolume().getValue(),
                order.getStatus()
        );
    }

    Order toDomain() {
        return Order.restore(
                id,
                new Location(locationX, locationY),
                Volume.mustCreate(volume),
                status
        ).getValueOrThrow();
    }
}
