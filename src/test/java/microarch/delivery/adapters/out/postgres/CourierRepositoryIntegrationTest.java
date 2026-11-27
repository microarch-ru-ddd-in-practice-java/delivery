package microarch.delivery.adapters.out.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import microarch.delivery.core.domain.model.courier.Assignment;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.courier.Status;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourierRepositoryIntegrationTest extends PostgresIntegrationTestBase {
  @Autowired
  CourierRepository repository;

  @Test
  void canSaveAndFindCourier() {
    var courier = Courier.create("Courier", Location.create(3, 3).getValue()).getValue();

    repository.save(courier);
    var loaded = repository.findById(courier.getId()).orElseThrow();

    assertThat(loaded.getId()).isEqualTo(courier.getId());
    assertThat(loaded.getName()).isEqualTo(courier.getName());
    assertThat(loaded.getLocation()).isEqualTo(courier.getLocation());
    assertThat(loaded.getMaxVolume()).isEqualTo(courier.getMaxVolume());
    assertThat(repository.findById(UUID.randomUUID())).isEmpty();
  }

  @Test
  void canUpdateCourierAndAssignments() {
    var location = Location.create(3, 3).getValue();
    var courier = Courier.create("Courier", location).getValue();
    var order = Order.create(UUID.randomUUID(), location, Volume.create(5).getValue()).getValue();
    courier.takeOrder(order).getOrElseThrow();
    repository.save(courier);
    var loaded = repository.findById(courier.getId()).orElseThrow();
    var assignment = (Assignment) ((List<?>) ReflectionTestUtils.getField(loaded, "assignments")).getFirst();
    assertThat(assignment.getOrderId()).isEqualTo(order.getId());
    assertThat(assignment.getVolume()).isEqualTo(order.getVolume());
    assertThat(assignment.getLocation()).isEqualTo(location);
    loaded.completeAssignment(assignment.getId()).getOrElseThrow();
    loaded.moveTo(Location.create(3, 4).getValue()).getOrElseThrow();

    repository.save(loaded);
    var updated = repository.findById(courier.getId()).orElseThrow();

    assertThat(updated.getLocation()).isEqualTo(loaded.getLocation());
    var updatedAssignments = (List<?>) ReflectionTestUtils.getField(updated, "assignments");
    assertThat(updatedAssignments).hasSize(1);
    var updatedAssignment = (Assignment) updatedAssignments.getFirst();
    assertThat(updatedAssignment.getId()).isEqualTo(assignment.getId());
    assertThat(updatedAssignment.getStatus()).isEqualTo(Status.COMPLETED);
  }

  @Test
  void canFindAllCouriers() {
    assertThat(repository.findAll()).isEmpty();
    var first = Courier.create("First", Location.create(3, 3).getValue()).getValue();
    var second = Courier.create("Second", Location.create(4, 4).getValue()).getValue();
    repository.save(first);
    repository.save(second);

    assertThat(repository.findAll()).extracting(Courier::getId)
        .containsExactlyInAnyOrder(first.getId(), second.getId());
  }
}
