package microarch.delivery.core.domain.model.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VolumeTest {

    // --- creation: valid range [0, 10] ---

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 5, 10 })
    void create_withValueInRange_succeeds(int value) {
        assertThat(Volume.create(value).isSuccess()).isTrue();
    }

    // --- creation: below min ---

    @ParameterizedTest
    @ValueSource(ints = { -1, -5 })
    void create_withValueBelowMin_fails(int value) {
        assertThat(Volume.create(value).isFailure()).isTrue();
    }

    // --- creation: above max ---

    @ParameterizedTest
    @ValueSource(ints = { 11, 50, 100 })
    void create_withValueAboveMax_fails(int value) {
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
