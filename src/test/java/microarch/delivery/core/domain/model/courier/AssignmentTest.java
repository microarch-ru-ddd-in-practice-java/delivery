package microarch.delivery.core.domain.model.courier;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Stream;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class AssignmentTest {

  @Test
  void shouldCreateAssignedAssignmentWithRequiredValues() {
    var orderId = UUID.randomUUID();
    var volume = Volume.create(3).getValue();
    var location = Location.create(5, 5).getValue();

    var result = Assignment.create(orderId, volume, location);

    assertThat(result.isSuccess()).isTrue();
    var assignment = result.getValue();
    assertThat(assignment.getId()).isNotNull().isNotEqualTo(new UUID(0, 0));
    assertThat(assignment.getOrderId()).isEqualTo(orderId);
    assertThat(assignment.getVolume()).isEqualTo(volume);
    assertThat(assignment.getLocation()).isEqualTo(location);
    assertThat(assignment.getStatus()).isEqualTo(Status.ASSIGNED);
  }

  @Test
  void shouldGenerateDifferentIdsForDifferentAssignments() {
    var first = createAssignment();
    var second = createAssignment();

    assertThat(first.getId()).isNotEqualTo(second.getId());
    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void shouldBeEqualWhenIdsMatchEvenIfOtherValuesDiffer() {
    var assignment = createAssignment();
    var other = Assignment.create(UUID.randomUUID(), Volume.create(7).getValue(),
        Location.create(2, 3).getValue()).getValue();
    ReflectionTestUtils.setField(other, "id", assignment.getId());

    assertThat(assignment).isNotSameAs(other);
    assertThat(assignment.getOrderId()).isNotEqualTo(other.getOrderId());
    assertThat(assignment.getVolume()).isNotEqualTo(other.getVolume());
    assertThat(assignment.getLocation()).isNotEqualTo(other.getLocation());
    assertThat(assignment).isEqualTo(other);
    assertThat(other).isEqualTo(assignment);
    assertThat(assignment.hashCode()).isEqualTo(other.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenIdsDifferEvenIfOtherValuesMatch() {
    var assignment = createAssignment();
    var other = Assignment.create(assignment.getOrderId(), assignment.getVolume(),
        assignment.getLocation()).getValue();

    assertThat(assignment).isNotEqualTo(other);
    assertThat(other).isNotEqualTo(assignment);
  }

  @Test
  void shouldBeEqualToItselfButNotNullOrAnotherType() {
    var assignment = createAssignment();

    assertThat(assignment.equals(assignment)).isTrue();
    assertThat(assignment.equals(null)).isFalse();
    assertThat(assignment.equals(assignment.getId())).isFalse();
  }

  @ParameterizedTest
  @MethodSource("nearbyCourierCoordinates")
  void shouldCompleteWhenCourierIsAtMostOneStepAway(int courierX, int courierY) {
    var assignment = createAssignment();
    var courierLocation = Location.create(courierX, courierY).getValue();

    var result = assignment.complete(courierLocation);

    assertThat(result.isSuccess()).isTrue();
    assertThat(assignment.getStatus()).isEqualTo(Status.COMPLETED);
  }

  @ParameterizedTest
  @MethodSource("distantCourierCoordinates")
  void shouldRemainAssignedWhenCourierIsTooFarAway(int courierX, int courierY) {
    var assignment = createAssignment();
    var courierLocation = Location.create(courierX, courierY).getValue();

    var result = assignment.complete(courierLocation);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("assignment.courier.faraway");
    assertThat(assignment.getStatus()).isEqualTo(Status.ASSIGNED);
  }

  @Test
  void shouldRejectRepeatedCompletionWithoutChangingCompletedStatus() {
    var assignment = createAssignment();
    assertThat(assignment.complete(assignment.getLocation()).isSuccess()).isTrue();

    var result = assignment.complete(assignment.getLocation());

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("assignment.completed");
    assertThat(assignment.getStatus()).isEqualTo(Status.COMPLETED);
  }

  @Test
  void shouldAllowCompletionAfterCourierMovesCloser() {
    var assignment = createAssignment();
    assertThat(assignment.complete(Location.create(1, 1).getValue()).isFailure()).isTrue();

    var result = assignment.complete(Location.create(5, 4).getValue());

    assertThat(result.isSuccess()).isTrue();
    assertThat(assignment.getStatus()).isEqualTo(Status.COMPLETED);
  }

  private static Assignment createAssignment() {
    return Assignment.create(UUID.randomUUID(), Volume.create(3).getValue(),
        Location.create(5, 5).getValue()).getValue();
  }

  private static Stream<Arguments> nullCreationArguments() {
    var orderId = UUID.randomUUID();
    var volume = Volume.create(3).getValue();
    var location = Location.create(5, 5).getValue();
    return Stream.of(
        Arguments.of(null, volume, location, "orderId"),
        Arguments.of(orderId, null, location, "volume"),
        Arguments.of(orderId, volume, null, "location"),
        Arguments.of(null, null, null, "orderId"));
  }

  private static Stream<Arguments> nearbyCourierCoordinates() {
    return Stream.of(
        Arguments.of(5, 5),
        Arguments.of(4, 5),
        Arguments.of(6, 5),
        Arguments.of(5, 4),
        Arguments.of(5, 6));
  }

  private static Stream<Arguments> distantCourierCoordinates() {
    return Stream.of(
        Arguments.of(4, 4),
        Arguments.of(4, 6),
        Arguments.of(6, 4),
        Arguments.of(6, 6),
        Arguments.of(3, 5),
        Arguments.of(7, 5),
        Arguments.of(5, 3),
        Arguments.of(5, 7),
        Arguments.of(1, 1),
        Arguments.of(10, 10));
  }
}
