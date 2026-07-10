package microarch.delivery.adapters.out.postgres;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import microarch.delivery.core.domain.model.courier.Assignment;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "couriers")
public class CourierJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Column(name = "max_volume", nullable = false)
    private int maxVolume;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id")
    private List<AssignmentJpaEntity> assignments = new ArrayList<>();

    protected CourierJpaEntity() {
    }

    private CourierJpaEntity(
            UUID id,
            String name,
            int locationX,
            int locationY,
            int maxVolume,
            List<AssignmentJpaEntity> assignments
    ) {
        this.id = id;
        this.name = name;
        this.locationX = locationX;
        this.locationY = locationY;
        this.maxVolume = maxVolume;
        this.assignments = assignments;
    }

    static CourierJpaEntity fromDomain(Courier courier) {
        Objects.requireNonNull(courier, "courier must not be null");

        return new CourierJpaEntity(
                courier.getId(),
                courier.getName(),
                courier.getLocation().getX(),
                courier.getLocation().getY(),
                courier.getMaxVolume().getValue(),
                courier.getAssignments().stream()
                        .map(AssignmentJpaEntity::fromDomain)
                        .toList()
        );
    }

    Courier toDomain() {
        List<Assignment> restoredAssignments = assignments.stream()
                .map(AssignmentJpaEntity::toDomain)
                .toList();

        return Courier.restore(
                id,
                name,
                new Location(locationX, locationY),
                Volume.mustCreate(maxVolume),
                restoredAssignments
        ).getValueOrThrow();
    }
}
