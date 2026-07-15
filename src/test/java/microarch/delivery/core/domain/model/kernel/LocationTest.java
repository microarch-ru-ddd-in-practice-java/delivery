package microarch.delivery.core.domain.model.kernel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationTest {

    // --- creation: valid ---

    @Test
    void create_withMinBoundary_succeeds() {
        assertThat(Location.create(1, 1).isSuccess()).isTrue();
    }

    @Test
    void create_withMaxBoundary_succeeds() {
        assertThat(Location.create(10, 10).isSuccess()).isTrue();
    }

    @Test
    void create_withMiddleValues_succeeds() {
        assertThat(Location.create(5, 7).isSuccess()).isTrue();
    }

    // --- creation: invalid x ---

    @Test
    void create_withXBelowMin_fails() {
        var result = Location.create(0, 5);
        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_withXAboveMax_fails() {
        var result = Location.create(11, 5);
        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_withNegativeX_fails() {
        var result = Location.create(-1, 5);
        assertThat(result.isFailure()).isTrue();
    }

    // --- creation: invalid y ---

    @Test
    void create_withYBelowMin_fails() {
        var result = Location.create(5, 0);
        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_withYAboveMax_fails() {
        var result = Location.create(5, 11);
        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_withNegativeY_fails() {
        var result = Location.create(5, -3);
        assertThat(result.isFailure()).isTrue();
    }

    // --- equality ---

    @Test
    void equals_sameCoordinates_isTrue() {
        var a = Location.create(3, 4).getValue();
        var b = Location.create(3, 4).getValue();
        assertThat(a).isEqualTo(b);
    }

    @Test
    void equals_differentX_isFalse() {
        var a = Location.create(3, 4).getValue();
        var b = Location.create(2, 4).getValue();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equals_differentY_isFalse() {
        var a = Location.create(3, 4).getValue();
        var b = Location.create(3, 5).getValue();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashCode_equalLocations_isSame() {
        var a = Location.create(6, 7).getValue();
        var b = Location.create(6, 7).getValue();
        assertThat(a).hasSameHashCodeAs(b);
    }

    // --- distance ---

    @Test
    void distanceTo_sameLocation_isZero() {
        var a = Location.create(5, 5).getValue();
        assertThat(a.distanceTo(a)).isZero();
    }

    @Test
    void distanceTo_horizontalOnly_isAbsDiffX() {
        var a = Location.create(2, 5).getValue();
        var b = Location.create(7, 5).getValue();
        assertThat(a.distanceTo(b)).isEqualTo(5);
    }

    @Test
    void distanceTo_verticalOnly_isAbsDiffY() {
        var a = Location.create(5, 2).getValue();
        var b = Location.create(5, 8).getValue();
        assertThat(a.distanceTo(b)).isEqualTo(6);
    }

    @Test
    void distanceTo_diagonal_isManhattanDistance() {
        var a = Location.create(1, 1).getValue();
        var b = Location.create(3, 4).getValue();
        // |3-1| + |4-1| = 2 + 3 = 5
        assertThat(a.distanceTo(b)).isEqualTo(5);
    }

    @Test
    void distanceTo_isSymmetric() {
        var a = Location.create(2, 3).getValue();
        var b = Location.create(7, 6).getValue();
        assertThat(a.distanceTo(b)).isEqualTo(b.distanceTo(a));
    }

    @Test
    void distanceTo_maxCorners_isMaxDistance() {
        var a = Location.create(1, 1).getValue();
        var b = Location.create(10, 10).getValue();
        // |10-1| + |10-1| = 9 + 9 = 18
        assertThat(a.distanceTo(b)).isEqualTo(18);
    }

    @Test
    void distanceTo_nullArgument_throwsNullPointerException() {
        var a = Location.create(5, 5).getValue();
        assertThatThrownBy(() -> a.distanceTo(null)).isInstanceOf(NullPointerException.class);
    }
}
