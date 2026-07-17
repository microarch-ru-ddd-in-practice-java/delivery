package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Инфраструктурный JPA-репозиторий для курьеров.
 *
 * <p>
 * Spring Data автоматически генерирует реализацию стандартных CRUD-операций.
 */
public interface CourierJpaRepository extends JpaRepository<Courier, UUID> {
}
