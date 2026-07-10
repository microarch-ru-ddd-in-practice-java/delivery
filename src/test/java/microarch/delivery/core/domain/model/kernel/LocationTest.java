package microarch.delivery.core.domain.model.kernel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Modifier;
import libs.errs.DomainInvariantException;
import org.junit.jupiter.api.Test;

class LocationTest {

    @Test
    void shouldCreateLocationWhenCoordinatesAreWithinAllowedRange() {
        Location minLocation = new Location(1, 1);
        Location maxLocation = new Location(10, 10);

        assertThat(minLocation.getX()).isEqualTo(1);
        assertThat(minLocation.getY()).isEqualTo(1);
        assertThat(maxLocation.getX()).isEqualTo(10);
        assertThat(maxLocation.getY()).isEqualTo(10);
    }

    @Test
    void shouldFailWhenCoordinateIsOutOfAllowedRange() {
        assertThatThrownBy(() -> new Location(0, 1)).isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(() -> new Location(11, 1)).isInstanceOf(DomainInvariantException.class);
    }

    @Test
    void shouldBeEqualWhenXAndYAreEqual() {
        Location first = new Location(3, 7);
        Location second = new Location(3, 7);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenAnyCoordinateIsDifferent() {
        Location location = new Location(3, 7);

        assertThat(location).isNotEqualTo(new Location(4, 7));
        assertThat(location).isNotEqualTo(new Location(3, 8));
    }

    @Test
    void shouldCalculateDistanceAsSumOfHorizontalAndVerticalSteps() {
        Location current = new Location(2, 3);
        Location destination = new Location(8, 10);

        assertThat(current.distanceTo(destination)).isEqualTo(13);
    }

    @Test
    void shouldBeImmutable() {
        assertThat(Location.class.getDeclaredFields())
                .filteredOn(field -> !field.isSynthetic())
                .allMatch(field -> Modifier.isFinal(field.getModifiers()));
    }
}
