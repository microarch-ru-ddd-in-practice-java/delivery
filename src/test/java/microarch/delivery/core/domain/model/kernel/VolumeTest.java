package microarch.delivery.core.domain.model.kernel;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeTest {

  @ParameterizedTest
  @MethodSource("validVolumes")
  void shouldCreatePositiveVolume(int value) {
    var result = Volume.create(value);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getValue().getValue()).isEqualTo(value);
  }

  @ParameterizedTest
  @MethodSource("invalidVolumes")
  void shouldRejectZeroAndNegativeVolumes(int value) {
    var result = Volume.create(value);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.getError().getCode()).isEqualTo("value.is.out.of.range");
  }

  @ParameterizedTest
  @MethodSource("validVolumes")
  void shouldUseValueAsEqualityComponent(int value) {
    var volume = Volume.create(value).getValue();

    assertThat(volume.equalityComponents()).containsExactly(value);
  }

  @ParameterizedTest
  @MethodSource("validVolumes")
  void shouldBeEqualWhenValuesMatch(int value) {
    var volume = Volume.create(value).getValue();
    var other = Volume.create(value).getValue();

    assertThat(volume).isNotSameAs(other);
    assertThat(volume).isEqualTo(other);
    assertThat(other).isEqualTo(volume);
    assertThat(volume.hashCode()).isEqualTo(other.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenValuesDiffer() {
    var volume = Volume.create(1).getValue();
    var other = Volume.create(2).getValue();

    assertThat(volume).isNotEqualTo(other);
    assertThat(other).isNotEqualTo(volume);
  }

  @Test
  void shouldNotBeEqualToNullOrAnotherType() {
    var volume = Volume.create(1).getValue();

    assertThat(volume.equals(null)).isFalse();
    assertThat(volume.equals(1)).isFalse();
  }

  private static Stream<Integer> validVolumes() {
    return Stream.of(1, 10, Integer.MAX_VALUE);
  }

  private static Stream<Integer> invalidVolumes() {
    return Stream.of(0, -1, Integer.MIN_VALUE);
  }
}
