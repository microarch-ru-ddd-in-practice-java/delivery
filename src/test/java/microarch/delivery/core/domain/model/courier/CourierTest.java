package microarch.delivery.core.domain.model.courier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class CourierTest {

  @Test
  void shouldCreateCourierWithNameLocationAndTwentyLiterCapacity() {
    var location = Location.create(5, 5).getValue();

    var result = Courier.create("Ivan", location);

    assertThat(result.isSuccess()).isTrue();
    var courier = result.getValue();
    assertThat(courier.getId()).isNotNull().isNotEqualTo(new UUID(0, 0));
    assertThat(courier.getName()).isEqualTo("Ivan");
    assertThat(courier.getLocation()).isEqualTo(location);
    assertThat(courier.getMaxVolume().getValue()).isEqualTo(20);
    assertThat(assignmentsOf(courier)).isEmpty();
  }

  @Test
  void shouldNotExposeAssignmentsThroughPublicGetter() {
    assertThatThrownBy(() -> Courier.class.getMethod("getAssignments"))
        .isInstanceOf(NoSuchMethodException.class);
  }

  @Test
  void shouldGenerateDifferentIdsForDifferentCouriers() {
    var location = Location.create(5, 5).getValue();

    var first = Courier.create("Ivan", location).getValue();
    var second = Courier.create("Ivan", location).getValue();

    assertThat(first.getId()).isNotEqualTo(second.getId());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "\n"})
  void shouldRejectMissingOrBlankName(String name) {
    var result = Courier.create(name, Location.create(5, 5).getValue());

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    assertThat(result.getError().getMessage()).isEqualTo("Value is required for name");
  }

  @Test
  void shouldRejectMissingLocation() {
    var result = Courier.create("Ivan", null);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    assertThat(result.getError().getMessage()).isEqualTo("Value is required for location");
  }

  @ParameterizedTest
  @MethodSource("capacityCases")
  void shouldCheckAndEnforceCapacity(int assignedVolume, int newVolume, boolean allowed) {
    var courier = createCourier(5, 5);
    if (assignedVolume > 0) {
      assertThat(courier.takeOrder(createOrder(assignedVolume, 5, 5)).isSuccess()).isTrue();
    }
    var existingAssignments = assignmentsOf(courier);
    var order = createOrder(newVolume, 5, 5);

    assertThat(courier.canTakeOrder(order)).isEqualTo(allowed);
    assertThat(assignmentsOf(courier)).containsExactlyElementsOf(existingAssignments);

    var result = courier.takeOrder(order);

    assertThat(result.isSuccess()).isEqualTo(allowed);
    if (allowed) {
      assertThat(assignmentsOf(courier)).hasSize(existingAssignments.size() + 1);
    } else {
      assertThat(result.getError().getCode()).isEqualTo("courier.max.volume.exceeded");
      assertThat(assignmentsOf(courier)).containsExactlyElementsOf(existingAssignments);
    }
  }

  @Test
  void shouldCountAllActiveAssignments() {
    var courier = createCourier(5, 5);
    assertThat(courier.takeOrder(createOrder(8, 5, 5)).isSuccess()).isTrue();
    assertThat(courier.takeOrder(createOrder(7, 5, 5)).isSuccess()).isTrue();
    assertThat(courier.takeOrder(createOrder(5, 5, 5)).isSuccess()).isTrue();

    assertThat(courier.canTakeOrder(createOrder(1, 5, 5))).isFalse();
    assertThat(courier.takeOrder(createOrder(1, 5, 5)).isFailure()).isTrue();
    assertThat(assignmentsOf(courier)).hasSize(3);
  }

  @Test
  void shouldCreateAssignmentWithOrderIdVolumeAndLocation() {
    var courier = createCourier(1, 1);
    var order = createOrder(3, 5, 5);

    var result = courier.takeOrder(order);

    assertThat(result.isSuccess()).isTrue();
    assertThat(assignmentsOf(courier)).hasSize(1);
    var assignment = assignmentsOf(courier).getFirst();
    assertThat(assignment.getOrderId()).isEqualTo(order.getId());
    assertThat(assignment.getVolume()).isEqualTo(order.getVolume());
    assertThat(assignment.getLocation()).isEqualTo(order.getLocation());
    assertThat(assignment.getStatus()).isEqualTo(Status.ASSIGNED);
    assertThat(assignment.getId()).isNotNull().isNotEqualTo(new UUID(0, 0));
  }

  @Test
  void shouldRejectNullOrder() {
    var courier = createCourier(5, 5);

    assertThat(courier.canTakeOrder(null)).isFalse();
    var result = courier.takeOrder(null);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    assertThat(assignmentsOf(courier)).isEmpty();
  }

  @Test
  void shouldRejectTakingTheSameOrderTwice() {
    var courier = createCourier(5, 5);
    var order = createOrder(3, 5, 5);
    assertThat(courier.takeOrder(order).isSuccess()).isTrue();

    var result = courier.takeOrder(order);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("courier.order.already.assigned");
    assertThat(assignmentsOf(courier)).hasSize(1);
  }

  @ParameterizedTest
  @MethodSource("nearbyCoordinates")
  void shouldCompleteAssignmentAtMostOneStepAway(int courierX, int courierY) {
    var courier = createCourier(courierX, courierY);
    assertThat(courier.takeOrder(createOrder(3, 5, 5)).isSuccess()).isTrue();
    var assignment = assignmentsOf(courier).getFirst();

    var result = courier.completeAssignment(assignment.getId());

    assertThat(result.isSuccess()).isTrue();
    assertThat(assignment.getStatus()).isEqualTo(Status.COMPLETED);
    assertThat(assignmentsOf(courier)).containsExactly(assignment);
  }

  @ParameterizedTest
  @MethodSource("distantCoordinates")
  void shouldRejectCompletionWhenTooFarAway(int courierX, int courierY) {
    var courier = createCourier(courierX, courierY);
    assertThat(courier.takeOrder(createOrder(20, 5, 5)).isSuccess()).isTrue();
    var assignment = assignmentsOf(courier).getFirst();

    var result = courier.completeAssignment(assignment.getId());

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("assignment.courier.faraway");
    assertThat(assignment.getStatus()).isEqualTo(Status.ASSIGNED);
    assertThat(courier.canTakeOrder(createOrder(1, 5, 5))).isFalse();
  }

  @Test
  void shouldReleaseOnlyCompletedAssignmentVolume() {
    var courier = createCourier(5, 5);
    var completedOrder = createOrder(8, 5, 5);
    assertThat(courier.takeOrder(completedOrder).isSuccess()).isTrue();
    assertThat(courier.takeOrder(createOrder(12, 5, 5)).isSuccess()).isTrue();
    assertThat(courier.completeAssignment(assignmentsOf(courier).getFirst().getId())
        .isSuccess()).isTrue();

    assertThat(courier.canTakeOrder(createOrder(9, 5, 5))).isFalse();
    assertThat(courier.canTakeOrder(createOrder(8, 5, 5))).isTrue();
    assertThat(courier.takeOrder(createOrder(8, 5, 5)).isSuccess()).isTrue();
    assertThat(assignmentsOf(courier)).hasSize(3);
    assertThat(courier.takeOrder(completedOrder).isFailure()).isTrue();
  }

  @Test
  void shouldRejectRepeatedCompletion() {
    var courier = createCourier(5, 5);
    assertThat(courier.takeOrder(createOrder(3, 5, 5)).isSuccess()).isTrue();
    var assignment = assignmentsOf(courier).getFirst();
    assertThat(courier.completeAssignment(assignment.getId()).isSuccess()).isTrue();

    var result = courier.completeAssignment(assignment.getId());

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("assignment.completed");
    assertThat(assignment.getStatus()).isEqualTo(Status.COMPLETED);
  }

  @Test
  void shouldRejectUnknownOrAnotherCouriersAssignment() {
    var courier = createCourier(5, 5);
    var otherCourier = createCourier(5, 5);
    assertThat(otherCourier.takeOrder(createOrder(3, 5, 5)).isSuccess()).isTrue();
    var foreignAssignment = assignmentsOf(otherCourier).getFirst();

    var foreignResult = courier.completeAssignment(foreignAssignment.getId());
    var unknownResult = courier.completeAssignment(UUID.randomUUID());

    assertThat(foreignResult.isFailure()).isTrue();
    assertThat(foreignResult.getError().getCode()).isEqualTo("record.not.found");
    assertThat(unknownResult.isFailure()).isTrue();
    assertThat(unknownResult.getError().getCode()).isEqualTo("record.not.found");
    assertThat(foreignAssignment.getStatus()).isEqualTo(Status.ASSIGNED);
  }

  @Test
  void shouldRejectMissingOrEmptyAssignmentId() {
    var courier = createCourier(5, 5);

    var missingResult = courier.completeAssignment(null);
    var emptyResult = courier.completeAssignment(new UUID(0, 0));

    assertThat(missingResult.isFailure()).isTrue();
    assertThat(missingResult.getError().getCode()).isEqualTo("value.is.required");
    assertThat(emptyResult.isFailure()).isTrue();
    assertThat(emptyResult.getError().getCode()).isEqualTo("value.is.required");
  }

  @ParameterizedTest
  @MethodSource("nearbyCoordinates")
  void shouldMoveAtMostOneStep(int destinationX, int destinationY) {
    var courier = createCourier(5, 5);
    var destination = Location.create(destinationX, destinationY).getValue();

    var result = courier.moveTo(destination);

    assertThat(result.isSuccess()).isTrue();
    assertThat(courier.getLocation()).isEqualTo(destination);
  }

  @ParameterizedTest
  @MethodSource("distantCoordinates")
  void shouldRejectMovingMoreThanOneStep(int destinationX, int destinationY) {
    var courier = createCourier(5, 5);
    var originalLocation = courier.getLocation();

    var result = courier.moveTo(Location.create(destinationX, destinationY).getValue());

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("courier.location.too.far");
    assertThat(courier.getLocation()).isEqualTo(originalLocation);
  }

  @Test
  void shouldRejectMissingDestination() {
    var courier = createCourier(5, 5);
    var originalLocation = courier.getLocation();

    var result = courier.moveTo(null);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.required");
    assertThat(courier.getLocation()).isEqualTo(originalLocation);
  }

  @ParameterizedTest
  @MethodSource("boundaryMovementCases")
  void shouldMoveAtGridBoundaries(int startX, int startY, int destinationX, int destinationY) {
    var courier = createCourier(startX, startY);
    var destination = Location.create(destinationX, destinationY).getValue();

    assertThat(courier.moveTo(destination).isSuccess()).isTrue();
    assertThat(courier.getLocation()).isEqualTo(destination);
  }

  @Test
  void shouldUseCurrentLocationForMovementAndCompletion() {
    var courier = createCourier(3, 5);
    var order = createOrder(20, 6, 5);
    assertThat(courier.takeOrder(order).isSuccess()).isTrue();
    var assignment = assignmentsOf(courier).getFirst();
    assertThat(courier.completeAssignment(assignment.getId()).isFailure()).isTrue();

    assertThat(courier.moveTo(Location.create(4, 5).getValue()).isSuccess()).isTrue();
    assertThat(courier.completeAssignment(assignment.getId()).isFailure()).isTrue();
    assertThat(courier.moveTo(Location.create(5, 5).getValue()).isSuccess()).isTrue();
    assertThat(courier.completeAssignment(assignment.getId()).isSuccess()).isTrue();

    assertThat(assignment.getLocation()).isEqualTo(order.getLocation());
    assertThat(assignment.getStatus()).isEqualTo(Status.COMPLETED);
    assertThat(courier.canTakeOrder(createOrder(20, 5, 5))).isTrue();
  }

  private static Stream<Arguments> capacityCases() {
    return Stream.of(
        Arguments.of(0, 1, true),
        Arguments.of(0, 20, true),
        Arguments.of(0, 21, false),
        Arguments.of(0, Integer.MAX_VALUE, false),
        Arguments.of(10, 10, true),
        Arguments.of(19, 1, true),
        Arguments.of(20, 1, false),
        Arguments.of(10, 11, false),
        Arguments.of(1, Integer.MAX_VALUE, false));
  }

  private static Stream<Arguments> nearbyCoordinates() {
    return Stream.of(
        Arguments.of(5, 5),
        Arguments.of(4, 5),
        Arguments.of(6, 5),
        Arguments.of(5, 4),
        Arguments.of(5, 6));
  }

  private static Stream<Arguments> distantCoordinates() {
    return Stream.of(
        Arguments.of(4, 4),
        Arguments.of(4, 6),
        Arguments.of(6, 4),
        Arguments.of(6, 6),
        Arguments.of(3, 5),
        Arguments.of(7, 5),
        Arguments.of(5, 3),
        Arguments.of(5, 7));
  }

  private static Stream<Arguments> boundaryMovementCases() {
    return Stream.of(
        Arguments.of(1, 1, 1, 2),
        Arguments.of(10, 10, 10, 9));
  }

  private static List<Assignment> assignmentsOf(Courier courier) {
    var assignments = (List<?>) ReflectionTestUtils.getField(courier, "assignments");
    assertThat(assignments).isNotNull();
    return assignments.stream().map(Assignment.class::cast).toList();
  }

  private static Courier createCourier(int locationX, int locationY) {
    return Courier.create("Ivan", Location.create(locationX, locationY).getValue()).getValue();
  }

  private static Order createOrder(int volume, int locationX, int locationY) {
    return Order.create(UUID.randomUUID(), Location.create(locationX, locationY).getValue(),
        Volume.create(volume).getValue()).getValue();
  }
}
