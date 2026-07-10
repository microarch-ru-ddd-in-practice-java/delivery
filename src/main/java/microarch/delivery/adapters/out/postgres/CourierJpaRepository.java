package microarch.delivery.adapters.out.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourierJpaRepository extends JpaRepository<CourierJpaEntity, UUID> {
}
