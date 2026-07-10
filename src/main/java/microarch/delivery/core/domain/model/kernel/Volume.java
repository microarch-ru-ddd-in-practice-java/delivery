package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;

import java.util.List;

public final class Volume extends ValueObject<Volume> {

    private final int value;

    private Volume(int value) {
        this.value = value;
    }

    public static Result<Volume, Error> create(int value) {
        Error validationError = Guard.againstLessOrEqual(value, 0, "volume");

        if (validationError != null) {
            return Result.failure(validationError);
        }

        return Result.success(new Volume(value));
    }

    public static Volume mustCreate(int value) {
        return create(value).getValueOrThrow();
    }

    public int getValue() {
        return value;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
