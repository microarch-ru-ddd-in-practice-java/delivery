package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Инфраструктурный JPA-репозиторий для заказов.
 *
 * <p>
 * Spring Data автоматически генерирует реализацию на основе сигнатур методов.
 */
public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    /**
     * Находит первый заказ с указанным статусом.
     *
     * @param status статус заказа
     * @return {@code Optional} с заказом, либо пустой {@code Optional}
     */
    Optional<Order> findFirstByStatus(OrderStatus status);

    /**
     * Возвращает все заказы с указанным статусом.
     *
     * @param status статус заказа
     * @return список заказов
     */
    List<Order> findAllByStatus(OrderStatus status);
}
