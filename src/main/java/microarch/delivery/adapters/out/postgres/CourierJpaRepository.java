package microarch.delivery.adapters.out.postgres;

import java.util.UUID;
import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierJpaRepository extends JpaRepository<Courier, UUID> {
}
