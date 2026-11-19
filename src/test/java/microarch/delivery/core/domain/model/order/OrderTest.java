package microarch.delivery.core.domain.model.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.stream.Stream;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class OrderTest {

  @Test
  void shouldCreateOrderWithProvidedIdAndCreatedStatus() {
    var id = UUID.randomUUID();
    var location = Location.create(5, 5).getValue();
    var volume = Volume.create(3).getValue();

    var result = Order.create(id, location, volume);

    assertThat(result.isSuccess()).isTrue();
    var order = result.getValue();
    assertThat(order.getId()).isEqualTo(id);
    assertThat(order.getLocation()).isEqualTo(location);
    assertThat(order.getVolume()).isEqualTo(volume);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
  }

  @ParameterizedTest
  @MethodSource("invalidCreationArguments")
  void shouldRejectMissingRequiredValues(UUID id, Location location, Volume volume,
      String parameterName) {
    var result = Order.create(id, location, volume);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    assertThat(result.getError().getMessage()).isEqualTo("Value is required for " + parameterName);
  }

  @ParameterizedTest
  @MethodSource("statusTransitions")
  void shouldAllowOnlySpecifiedStatusTransitions(OrderStatus initialStatus,
      OrderStatus targetStatus, boolean allowed) {
    var order = createOrderWithStatus(initialStatus);
    var id = order.getId();
    var location = order.getLocation();
    var volume = order.getVolume();

    var result = order.changeStatus(targetStatus);

    assertThat(result.isSuccess()).isEqualTo(allowed);
    assertThat(order.getStatus()).isEqualTo(allowed ? targetStatus : initialStatus);
    assertThat(order.getId()).isEqualTo(id);
    assertThat(order.getLocation()).isEqualTo(location);
    assertThat(order.getVolume()).isEqualTo(volume);
    if (!allowed) {
      assertThat(result.getError().getCode()).isEqualTo("order.status.invalid.transition");
    }
  }

  @ParameterizedTest
  @EnumSource(OrderStatus.class)
  void shouldRejectNullStatusWithoutChangingCurrentStatus(OrderStatus initialStatus) {
    var order = createOrderWithStatus(initialStatus);

    var result = order.changeStatus(null);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    assertThat(result.getError().getMessage()).isEqualTo("Value is required for status");
    assertThat(order.getStatus()).isEqualTo(initialStatus);
  }

  private static Order createOrderWithStatus(OrderStatus status) {
    var order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(),
        Volume.create(3).getValue()).getValue();
    if (status != OrderStatus.CREATED) {
      assertThat(order.changeStatus(OrderStatus.ASSIGNED).isSuccess()).isTrue();
    }
    if (status == OrderStatus.COMPLETED) {
      assertThat(order.changeStatus(OrderStatus.COMPLETED).isSuccess()).isTrue();
    }
    return order;
  }

  private static Stream<Arguments> invalidCreationArguments() {
    var id = UUID.randomUUID();
    var location = Location.create(5, 5).getValue();
    var volume = Volume.create(3).getValue();
    return Stream.of(
        Arguments.of(null, location, volume, "id"),
        Arguments.of(new UUID(0, 0), location, volume, "id"),
        Arguments.of(id, null, volume, "location"),
        Arguments.of(id, location, null, "volume"),
        Arguments.of(null, null, null, "id"));
  }

  private static Stream<Arguments> statusTransitions() {
    return Stream.of(
        Arguments.of(OrderStatus.CREATED, OrderStatus.CREATED, false),
        Arguments.of(OrderStatus.CREATED, OrderStatus.ASSIGNED, true),
        Arguments.of(OrderStatus.CREATED, OrderStatus.COMPLETED, false),
        Arguments.of(OrderStatus.ASSIGNED, OrderStatus.CREATED, false),
        Arguments.of(OrderStatus.ASSIGNED, OrderStatus.ASSIGNED, false),
        Arguments.of(OrderStatus.ASSIGNED, OrderStatus.COMPLETED, true),
        Arguments.of(OrderStatus.COMPLETED, OrderStatus.CREATED, false),
        Arguments.of(OrderStatus.COMPLETED, OrderStatus.ASSIGNED, false),
        Arguments.of(OrderStatus.COMPLETED, OrderStatus.COMPLETED, false));
  }
}
