package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.out.OrderRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void save(Order order) {
        jpa.save(order);
    }

    @Override
    @Transactional
    public void update(Order order) {
        jpa.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findFirstCreated() {
        return jpa.findFirstByStatus(OrderStatus.CREATED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllAssigned() {
        return jpa.findAllByStatus(OrderStatus.ASSIGNED);
    }
}
