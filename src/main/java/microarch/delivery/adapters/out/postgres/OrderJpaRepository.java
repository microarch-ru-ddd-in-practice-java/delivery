package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findFirstByStatus(OrderStatus status);

    List<OrderJpaEntity> findAllByStatus(OrderStatus status);
}
