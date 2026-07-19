package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.out.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_3;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    OrderRepository repository;

    @Test
    void CanAddOrder() {
        var order = Order.create(UUID.randomUUID(), L_5_5, VOLUME_3).getValueOrThrow();

        repository.save(order);
        var loaded = repository.findById(order.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getStatus()).isEqualTo(order.getStatus());
        assertThat(loaded.get().getLocation()).isEqualTo(order.getLocation());
        assertThat(loaded.get().getVolume()).isEqualTo(order.getVolume());
    }

    @Test
    void CanUpdateOrder() {
        var order = Order.create(UUID.randomUUID(), L_5_5, VOLUME_3).getValueOrThrow();
        repository.save(order);

        order.assign().getOrElseThrow();
        repository.update(order);

        var loaded = repository.findById(order.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void CanGetFirstCreatedOrder() {
        var order = Order.create(UUID.randomUUID(), L_5_5, VOLUME_3).getValueOrThrow();
        repository.save(order);

        var found = repository.findFirstCreated();

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void CanGetFirstCreatedOrder_whenNoneExists() {
        var found = repository.findFirstCreated();

        assertThat(found).isEmpty();
    }

    @Test
    void CanGetAllAssignedOrders() {
        var created = Order.create(UUID.randomUUID(), L_5_5, VOLUME_3).getValueOrThrow();
        var assigned = Order.create(UUID.randomUUID(), L_5_5, VOLUME_3).getValueOrThrow();
        assigned.assign().getOrElseThrow();

        repository.save(created);
        repository.save(assigned);

        var result = repository.findAllAssigned();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }
}
