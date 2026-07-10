package microarch.delivery.adapters.out.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import microarch.delivery.core.domain.model.courier.Assignment;
import microarch.delivery.core.domain.model.courier.AssignmentStatus;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "assignments")
public class AssignmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Column(name = "volume", nullable = false)
    private int volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status;

    protected AssignmentJpaEntity() {
    }

    private AssignmentJpaEntity(
            UUID id,
            UUID orderId,
            int locationX,
            int locationY,
            int volume,
            AssignmentStatus status
    ) {
        this.id = id;
        this.orderId = orderId;
        this.locationX = locationX;
        this.locationY = locationY;
        this.volume = volume;
        this.status = status;
    }

    static AssignmentJpaEntity fromDomain(Assignment assignment) {
        Objects.requireNonNull(assignment, "assignment must not be null");

        return new AssignmentJpaEntity(
                assignment.getId(),
                assignment.getOrderId(),
                assignment.getLocation().getX(),
                assignment.getLocation().getY(),
                assignment.getVolume().getValue(),
                assignment.getStatus()
        );
    }

    Assignment toDomain() {
        return Assignment.restore(
                id,
                orderId,
                Volume.mustCreate(volume),
                new Location(locationX, locationY),
                status
        ).getValueOrThrow();
    }
}
