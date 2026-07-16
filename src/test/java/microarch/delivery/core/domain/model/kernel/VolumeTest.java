package microarch.delivery.core.domain.model.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VolumeTest {

    // --- creation: valid (>= 0, no upper bound) ---

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 5, 100, 1000 })
    void create_withNonNegativeValue_succeeds(int value) {
        assertThat(Volume.create(value).isSuccess()).isTrue();
    }

    // --- creation: invalid (negative) ---

    @ParameterizedTest
    @ValueSource(ints = { -1, -5, -100 })
    void create_withNegativeValue_fails(int value) {
        assertThat(Volume.create(value).isFailure()).isTrue();
    }

    // --- equality ---

    @Test
    void equals_sameValue_isTrue() {
        var a = Volume.create(3).getValue();
        var b = Volume.create(3).getValue();
        assertThat(a).isEqualTo(b);
    }

    @Test
    void equals_differentValue_isFalse() {
        var a = Volume.create(2).getValue();
        var b = Volume.create(4).getValue();
        assertThat(a).isNotEqualTo(b);
    }
}
