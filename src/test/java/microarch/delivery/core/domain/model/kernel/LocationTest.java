package microarch.delivery.core.domain.model.kernel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class LocationTest {

    @ParameterizedTest
    @MethodSource("validCoordinates")
    void shouldStoreValidCoordinates(int x, int y) {
        var result = Location.create(x, y);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(x);
        assertThat(result.getValue().getY()).isEqualTo(y);
    }

    @ParameterizedTest
    @MethodSource("coordinatesOutsideBoard")
    void shouldRejectCoordinatesOutsideBoard(int x, int y) {
        var result = Location.create(x, y);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.is.out.of.range");
    }

    @ParameterizedTest
    @MethodSource("validCoordinates")
    void shouldUseBothCoordinatesAsEqualityComponents(int x, int y) {
        var location = Location.create(x, y).getValue();

        assertThat(location.equalityComponents()).containsExactly(x, y);
    }

    @ParameterizedTest
    @MethodSource("validCoordinates")
    void shouldBeEqualWhenBothCoordinatesMatch(int x, int y) {
        var location = Location.create(x, y).getValue();
        var other = Location.create(x, y).getValue();

        assertThat(location).isNotSameAs(other);
        assertThat(location).isEqualTo(other);
        assertThat(other).isEqualTo(location);
        assertThat(location.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    void shouldTreatEqualLocationsAsOneHashSetEntry() {
        var location = Location.create(3, 7).getValue();
        var equalLocation = Location.create(3, 7).getValue();
        var differentLocation = Location.create(4, 7).getValue();
        var locations = new HashSet<Location>();

        locations.add(location);
        locations.add(equalLocation);
        locations.add(differentLocation);

        assertThat(locations).hasSize(2).contains(location, differentLocation);
    }

    @ParameterizedTest
    @MethodSource("differentCoordinates")
    void shouldNotBeEqualWhenEitherCoordinateDiffers(int otherX, int otherY) {
        var location = Location.create(3, 7).getValue();
        var other = Location.create(otherX, otherY).getValue();

        assertThat(location).isNotEqualTo(other);
        assertThat(other).isNotEqualTo(location);
    }

    @Test
    void shouldBeEqualToItself() {
        var location = Location.create(3, 7).getValue();

        assertThat(location.equals(location)).isTrue();
    }

    @Test
    void shouldNotBeEqualToNull() {
        var location = Location.create(3, 7).getValue();

        assertThat(location.equals(null)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("distanceCases")
    void shouldCalculateHorizontalAndVerticalSteps(int fromX, int fromY, int toX, int toY,
            int expectedDistance) {
        var from = Location.create(fromX, fromY).getValue();
        var to = Location.create(toX, toY).getValue();

        assertThat(from.distanceTo(to)).isEqualTo(expectedDistance);
        assertThat(to.distanceTo(from)).isEqualTo(expectedDistance);
    }

    @Test
    void shouldRejectNullDistanceTarget() {
        var location = Location.create(1, 1).getValue();

        assertThatNullPointerException().isThrownBy(() -> location.distanceTo(null));
    }

    private static Stream<Arguments> validCoordinates() {
        return Stream.of(
                Arguments.of(3, 7),
                Arguments.of(1, 1),
                Arguments.of(10, 10));
    }

    private static Stream<Arguments> oppositeBoardCorners() {
        return Stream.of(
                Arguments.of(1, 10),
                Arguments.of(10, 1));
    }

    private static Stream<Arguments> coordinatesOutsideBoard() {
        return Stream.of(
                Arguments.of(0, 5),
                Arguments.of(-1, 5),
                Arguments.of(Integer.MIN_VALUE, 5),
                Arguments.of(5, 0),
                Arguments.of(5, -1),
                Arguments.of(5, Integer.MIN_VALUE),
                Arguments.of(11, 5),
                Arguments.of(12, 5),
                Arguments.of(Integer.MAX_VALUE, 5),
                Arguments.of(5, 11),
                Arguments.of(5, 12),
                Arguments.of(5, Integer.MAX_VALUE),
                Arguments.of(0, 0),
                Arguments.of(11, 11));
    }

    private static Stream<Arguments> differentCoordinates() {
        return Stream.of(
                Arguments.of(4, 7),
                Arguments.of(3, 8),
                Arguments.of(4, 8));
    }

    private static Stream<Arguments> distanceCases() {
        return Stream.of(
                Arguments.of(1, 1, 3, 4, 5),
                Arguments.of(3, 4, 1, 1, 5),
                Arguments.of(1, 4, 3, 1, 5),
                Arguments.of(3, 1, 1, 4, 5),
                Arguments.of(2, 3, 7, 3, 5),
                Arguments.of(2, 3, 2, 8, 5),
                Arguments.of(5, 5, 5, 5, 0),
                Arguments.of(1, 1, 10, 10, 18),
                Arguments.of(1, 10, 10, 1, 18));
    }
}
