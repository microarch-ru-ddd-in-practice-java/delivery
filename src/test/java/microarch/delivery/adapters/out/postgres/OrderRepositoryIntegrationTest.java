package microarch.delivery.adapters.out.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderRepositoryIntegrationTest extends PostgresIntegrationTestBase {
  @Autowired
  OrderRepository repository;

  @Test
  void canSaveAndFindOrder() {
    var order = createOrder();

    repository.save(order);
    var loaded = repository.findById(order.getId()).orElseThrow();

    assertThat(loaded.getId()).isEqualTo(order.getId());
    assertThat(loaded.getLocation()).isEqualTo(order.getLocation());
    assertThat(loaded.getVolume()).isEqualTo(order.getVolume());
    assertThat(loaded.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(repository.findById(UUID.randomUUID())).isEmpty();
  }

  @Test
  void canUpdateOrder() {
    var order = createOrder();
    repository.save(order);
    var loaded = repository.findById(order.getId()).orElseThrow();
    loaded.changeStatus(OrderStatus.ASSIGNED).getOrElseThrow();

    repository.save(loaded);

    assertThat(repository.findById(order.getId()).orElseThrow().getStatus())
        .isEqualTo(OrderStatus.ASSIGNED);
  }

  @Test
  void canFindAnyCreatedOrder() {
    assertThat(repository.findAnyCreated()).isEmpty();
    var created = createOrder();
    var assigned = createOrder();
    assigned.changeStatus(OrderStatus.ASSIGNED).getOrElseThrow();
    repository.save(assigned);
    assertThat(repository.findAnyCreated()).isEmpty();
    repository.save(created);

    var loaded = repository.findAnyCreated().orElseThrow();

    assertThat(loaded.getId()).isEqualTo(created.getId());
    assertThat(loaded.getStatus()).isEqualTo(OrderStatus.CREATED);
  }

  @Test
  void canFindAllAssignedOrders() {
    assertThat(repository.findAllAssigned()).isEmpty();
    var first = createOrder();
    var second = createOrder();
    var completed = createOrder();
    first.changeStatus(OrderStatus.ASSIGNED).getOrElseThrow();
    second.changeStatus(OrderStatus.ASSIGNED).getOrElseThrow();
    completed.changeStatus(OrderStatus.ASSIGNED).getOrElseThrow();
    completed.changeStatus(OrderStatus.COMPLETED).getOrElseThrow();
    repository.save(first);
    repository.save(second);
    repository.save(completed);
    repository.save(createOrder());

    assertThat(repository.findAllAssigned()).extracting(Order::getId)
        .containsExactlyInAnyOrder(first.getId(), second.getId());
  }

  private static Order createOrder() {
    return Order.create(UUID.randomUUID(), Location.create(3, 4).getValue(), Volume.create(5).getValue())
        .getValue();
  }
}
