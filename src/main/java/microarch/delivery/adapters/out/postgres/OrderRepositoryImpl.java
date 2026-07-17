package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.out.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Адаптер репозитория заказов, реализующий порт {@link OrderRepository} через Spring Data JPA.
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpa;

    public OrderRepositoryImpl(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Order order) {
        jpa.save(order);
    }

    @Override
    public void update(Order order) {
        jpa.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Order> findFirstCreated() {
        return jpa.findFirstByStatus(OrderStatus.CREATED);
    }

    @Override
    public List<Order> findAllAssigned() {
        return jpa.findAllByStatus(OrderStatus.ASSIGNED);
    }
}
