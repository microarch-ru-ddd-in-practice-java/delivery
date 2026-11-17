package microarch.delivery.core.domain.model.kernel;

import java.util.List;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Volume extends ValueObject<Volume> {

  private static final int MIN_VOLUME = 1;

  private final int value;

  public static Result<Volume, Error> create(int value) {
    var error = Guard.againstOutOfRange(value, MIN_VOLUME, Integer.MAX_VALUE, "volume");
    if (error != null) {
      return Result.failure(error);
    }
    return Result.success(new Volume(value));
  }

  @Override
  protected Iterable<Object> equalityComponents() {
    return List.of(this.value);
  }
}
