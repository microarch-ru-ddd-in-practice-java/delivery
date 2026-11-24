package microarch.delivery.core.domain.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import microarch.delivery.core.domain.model.courier.Assignment;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.courier.Status;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

class DispatchServiceTest {

  private final DispatchService service = new DispatchService();

  @ParameterizedTest
  @MethodSource("courierLocations")
  void shouldChooseNearestEligibleCourier(int firstX, int firstY, int secondX, int secondY,
      int winnerIndex) {
    var order = createOrder(3, 5, 5);
    var couriers = List.of(createCourier(firstX, firstY), createCourier(secondX, secondY));

    var result = service.dispatch(order, couriers);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getValue()).isSameAs(couriers.get(winnerIndex));
    assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    var assignments = assignmentsOf(result.getValue());
    assertThat(assignments).hasSize(1);
    var assignment = assignments.getFirst();
    assertThat(assignment.getOrderId()).isEqualTo(order.getId());
    assertThat(assignment.getLocation()).isEqualTo(order.getLocation());
    assertThat(assignment.getVolume()).isEqualTo(order.getVolume());
    assertThat(assignment.getStatus()).isEqualTo(Status.ASSIGNED);
    assertThat(assignmentsOf(couriers.get(1 - winnerIndex))).isEmpty();
  }

  @Test
  void shouldKeepCourierOrderAndLocationsUnchanged() {
    var first = createCourier(1, 1);
    var second = createCourier(5, 4);
    var couriers = new ArrayList<>(List.of(first, second));
    var order = createOrder(3, 5, 5);
    var orderId = order.getId();
    var orderLocation = order.getLocation();
    var orderVolume = order.getVolume();
    var firstLocation = first.getLocation();
    var secondLocation = second.getLocation();

    var result = service.dispatch(order, couriers);

    assertThat(result.isSuccess()).isTrue();
    assertThat(couriers).containsExactly(first, second);
    assertThat(first.getLocation()).isEqualTo(firstLocation);
    assertThat(second.getLocation()).isEqualTo(secondLocation);
    assertThat(order.getId()).isEqualTo(orderId);
    assertThat(order.getLocation()).isEqualTo(orderLocation);
    assertThat(order.getVolume()).isEqualTo(orderVolume);
  }

  @ParameterizedTest
  @MethodSource("insufficientCapacityCases")
  void shouldSkipNearestCourierWithoutEnoughRemainingCapacity(int assignedVolume,
      int newVolume) {
    var nearest = createCourier(5, 5);
    var farther = createCourier(1, 1);
    assertThat(nearest.takeOrder(createOrder(assignedVolume, 5, 5)).isSuccess()).isTrue();
    var existingAssignments = assignmentsOf(nearest);
    var order = createOrder(newVolume, 5, 5);

    var result = service.dispatch(order, List.of(nearest, farther));

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getValue()).isSameAs(farther);
    assertThat(assignmentsOf(nearest)).containsExactlyElementsOf(existingAssignments);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
  }

  @ParameterizedTest
  @MethodSource("fittingCapacityCases")
  void shouldAllowMultipleOrdersUpToTwentyLiters(int assignedVolume, int newVolume) {
    var nearest = createCourier(5, 5);
    var farther = createCourier(1, 1);
    var previousOrder = createOrder(assignedVolume, 5, 5);
    assertThat(service.dispatch(previousOrder, List.of(nearest)).isSuccess()).isTrue();
    var previousAssignment = assignmentsOf(nearest).getFirst();
    var order = createOrder(newVolume, 5, 5);

    var result = service.dispatch(order, List.of(farther, nearest));

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getValue()).isSameAs(nearest);
    assertThat(assignmentsOf(nearest)).hasSize(2).contains(previousAssignment);
    assertThat(previousOrder.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    assertThat(assignmentsOf(farther)).isEmpty();
  }

  @Test
  void shouldNotLimitNumberOfOrdersWhileVolumeFits() {
    var courier = createCourier(5, 5);

    for (int orderIndex = 0; orderIndex < 20; orderIndex++) {
      var order = createOrder(1, 5, 5);
      var result = service.dispatch(order, List.of(courier));

      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getValue()).isSameAs(courier);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }
    var extraOrder = createOrder(1, 5, 5);

    var result = service.dispatch(extraOrder, List.of(courier));

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("dispatch.courier.not.found");
    assertThat(assignmentsOf(courier)).hasSize(20);
    assertThat(extraOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
  }

  @Test
  void shouldUseCurrentCourierLocationAfterMovement() {
    var movedCourier = createCourier(5, 3);
    var otherCourier = createCourier(4, 4);
    assertThat(movedCourier.moveTo(Location.create(5, 4).getValue()).isSuccess()).isTrue();
    var order = createOrder(3, 5, 5);

    var result = service.dispatch(order, List.of(otherCourier, movedCourier));

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getValue()).isSameAs(movedCourier);
    assertThat(assignmentsOf(otherCourier)).isEmpty();
  }

  @Test
  void shouldReuseCapacityReleasedByCompletedAssignments() {
    var courier = createCourier(5, 5);
    assertThat(service.dispatch(createOrder(20, 5, 5), List.of(courier)).isSuccess()).isTrue();
    var previousAssignment = assignmentsOf(courier).getFirst();
    assertThat(courier.completeAssignment(previousAssignment.getId()).isSuccess()).isTrue();
    var order = createOrder(20, 5, 5);

    var result = service.dispatch(order, List.of(courier));

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getValue()).isSameAs(courier);
    assertThat(assignmentsOf(courier)).hasSize(2);
    assertThat(previousAssignment.getStatus()).isEqualTo(Status.COMPLETED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
  }

  @ParameterizedTest
  @MethodSource("unavailableCouriers")
  void shouldFailWithoutChangingOrderOrCouriersWhenNobodyCanTakeOrder(List<Courier> couriers) {
    var order = createOrder(10, 5, 5);
    var existingAssignments = couriers == null ? List.of()
        : couriers.stream().map(courier -> assignmentsOf(courier)).toList();

    var result = service.dispatch(order, couriers);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("dispatch.courier.not.found");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    if (couriers != null) {
      assertThat(couriers.stream().map(courier -> assignmentsOf(courier)).toList())
          .isEqualTo(existingAssignments);
    }
  }

  @ParameterizedTest
  @MethodSource("oversizedOrderVolumes")
  void shouldRejectOrderLargerThanAnyCourierCapacity(int volume) {
    var courier = createCourier(5, 5);
    var order = createOrder(volume, 5, 5);

    var result = service.dispatch(order, List.of(courier));

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("dispatch.courier.not.found");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(assignmentsOf(courier)).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("nonCreatedStatuses")
  void shouldRejectNonCreatedOrderWithoutChangingCourier(OrderStatus status) {
    var courier = createCourier(5, 5);
    var order = createOrder(3, 5, 5);
    assertThat(order.changeStatus(OrderStatus.ASSIGNED).isSuccess()).isTrue();
    if (status == OrderStatus.COMPLETED) {
      assertThat(order.changeStatus(OrderStatus.COMPLETED).isSuccess()).isTrue();
    }

    var result = service.dispatch(order, List.of(courier));

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("dispatch.order.not.created");
    assertThat(order.getStatus()).isEqualTo(status);
    assertThat(assignmentsOf(courier)).isEmpty();
  }

  @Test
  void shouldRejectNullOrderWithoutChangingCourier() {
    var courier = createCourier(5, 5);

    var result = service.dispatch(null, List.of(courier));

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    assertThat(assignmentsOf(courier)).isEmpty();
  }

  @Test
  void shouldPropagateAssignmentFailureWithoutChangingOrderOrOtherCouriers() {
    var nearest = createCourier(5, 5);
    var farther = createCourier(1, 1);
    var order = createOrder(3, 5, 5);
    assertThat(nearest.takeOrder(order).isSuccess()).isTrue();
    var existingAssignment = assignmentsOf(nearest).getFirst();

    var result = service.dispatch(order, List.of(nearest, farther));

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("courier.order.already.assigned");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(assignmentsOf(nearest)).containsExactly(existingAssignment);
    assertThat(assignmentsOf(farther)).isEmpty();
  }

  @Test
  void shouldNotDispatchTheSameOrderTwice() {
    var courier = createCourier(5, 5);
    var order = createOrder(3, 5, 5);
    assertThat(service.dispatch(order, List.of(courier)).isSuccess()).isTrue();
    var assignment = assignmentsOf(courier).getFirst();

    var result = service.dispatch(order, List.of(courier));

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("dispatch.order.not.created");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    assertThat(assignmentsOf(courier)).containsExactly(assignment);
  }

  private static Stream<Arguments> courierLocations() {
    return Stream.of(
        Arguments.of(1, 1, 5, 4, 1),
        Arguments.of(5, 4, 1, 1, 0),
        Arguments.of(5, 4, 5, 5, 1),
        Arguments.of(5, 4, 4, 5, 0),
        Arguments.of(5, 5, 5, 5, 0),
        Arguments.of(7, 7, 5, 8, 1),
        Arguments.of(1, 1, 10, 10, 0));
  }

  private static Stream<Arguments> insufficientCapacityCases() {
    return Stream.of(Arguments.of(20, 1), Arguments.of(18, 3));
  }

  private static Stream<Arguments> fittingCapacityCases() {
    return Stream.of(Arguments.of(10, 10), Arguments.of(10, 9), Arguments.of(19, 1));
  }

  private static Stream<Arguments> unavailableCouriers() {
    var full = createCourier(5, 5);
    var insufficientSpace = createCourier(1, 1);
    assertThat(full.takeOrder(createOrder(20, 5, 5)).isSuccess()).isTrue();
    assertThat(insufficientSpace.takeOrder(createOrder(11, 1, 1)).isSuccess()).isTrue();
    return Stream.of(
        Arguments.of((Object) null),
        Arguments.of(List.of()),
        Arguments.of(List.of(full)),
        Arguments.of(List.of(full, insufficientSpace)));
  }

  private static Stream<Integer> oversizedOrderVolumes() {
    return Stream.of(21, Integer.MAX_VALUE);
  }

  private static Stream<OrderStatus> nonCreatedStatuses() {
    return Stream.of(OrderStatus.ASSIGNED, OrderStatus.COMPLETED);
  }

  private static List<Assignment> assignmentsOf(Courier courier) {
    var assignments = (List<?>) ReflectionTestUtils.getField(courier, "assignments");
    assertThat(assignments).isNotNull();
    return assignments.stream().map(assignment -> Assignment.class.cast(assignment)).toList();
  }

  private static Courier createCourier(int locationX, int locationY) {
    return Courier.create("Ivan", Location.create(locationX, locationY).getValue()).getValue();
  }

  private static Order createOrder(int volume, int locationX, int locationY) {
    return Order.create(UUID.randomUUID(), Location.create(locationX, locationY).getValue(),
        Volume.create(volume).getValue()).getValue();
  }
}
