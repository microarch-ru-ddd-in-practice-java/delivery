package microarch.delivery.core.domain.model.kernel;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  @ParameterizedTest
  @MethodSource("volumeSums")
  void shouldAddVolumesWithoutChangingOperands(int firstValue, int secondValue, int expectedValue) {
    var volume = Volume.create(firstValue).getValue();
    var other = Volume.create(secondValue).getValue();

    var result = volume.add(other);

    assertThat(result).isEqualTo(Volume.create(expectedValue).getValue());
    assertThat(result).isNotSameAs(volume).isNotSameAs(other);
    assertThat(volume.getValue()).isEqualTo(firstValue);
    assertThat(other.getValue()).isEqualTo(secondValue);
  }

  @ParameterizedTest
  @MethodSource("overflowingVolumeSums")
  void shouldRejectAdditionOverflow(int firstValue, int secondValue) {
    var volume = Volume.create(firstValue).getValue();
    var other = Volume.create(secondValue).getValue();

    assertThatThrownBy(() -> volume.add(other)).isInstanceOf(ArithmeticException.class);
    assertThat(volume.getValue()).isEqualTo(firstValue);
    assertThat(other.getValue()).isEqualTo(secondValue);
  }

  @Test
  void shouldRejectAddingNullVolume() {
    var volume = Volume.create(1).getValue();

    assertThatNullPointerException().isThrownBy(() -> volume.add(null));
  }

  @ParameterizedTest
  @MethodSource("volumeComparisons")
  void shouldCompareVolumes(int firstValue, int secondValue, boolean expected) {
    var volume = Volume.create(firstValue).getValue();
    var other = Volume.create(secondValue).getValue();

    assertThat(volume.isGreaterThan(other)).isEqualTo(expected);
  }

  @Test
  void shouldRejectComparingWithNullVolume() {
    var volume = Volume.create(1).getValue();

    assertThatNullPointerException().isThrownBy(() -> volume.isGreaterThan(null));
  }

  private static Stream<Arguments> volumeSums() {
    return Stream.of(
        Arguments.of(1, 1, 2),
        Arguments.of(8, 12, 20),
        Arguments.of(Integer.MAX_VALUE - 1, 1, Integer.MAX_VALUE),
        Arguments.of(1, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
  }

  private static Stream<Arguments> overflowingVolumeSums() {
    return Stream.of(
        Arguments.of(Integer.MAX_VALUE, 1),
        Arguments.of(1, Integer.MAX_VALUE),
        Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }

  private static Stream<Arguments> volumeComparisons() {
    return Stream.of(
        Arguments.of(20, 19, true),
        Arguments.of(20, 20, false),
        Arguments.of(19, 20, false),
        Arguments.of(1, 1, false),
        Arguments.of(Integer.MAX_VALUE, 1, true),
        Arguments.of(1, Integer.MAX_VALUE, false),
        Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE, false));
  }

  private static Stream<Integer> validVolumes() {
    return Stream.of(1, 10, Integer.MAX_VALUE);
  }

  private static Stream<Integer> invalidVolumes() {
    return Stream.of(0, -1, Integer.MIN_VALUE);
  }
}
