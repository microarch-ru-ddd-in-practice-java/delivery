package microarch.delivery.core.domain.model.kernel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.List;
import java.util.Objects;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Volume extends ValueObject<Volume> {

  private static final int MIN_VOLUME = 1;

  @Column(name = "volume", nullable = false)
  private final int value;

  public static Result<Volume, Error> create(int value) {
    var error = Guard.againstOutOfRange(value, MIN_VOLUME, Integer.MAX_VALUE, "volume");
    if (error != null) {
      return Result.failure(error);
    }
    return Result.success(new Volume(value));
  }

  public Volume add(Volume other) {
    Objects.requireNonNull(other, "other volume is null");
    return new Volume(Math.addExact(value, other.value));
  }

  public boolean isGreaterThan(Volume other) {
    Objects.requireNonNull(other, "other volume is null");
    return value > other.value;
  }

  @Override
  protected Iterable<Object> equalityComponents() {
    return List.of(this.value);
  }
}
