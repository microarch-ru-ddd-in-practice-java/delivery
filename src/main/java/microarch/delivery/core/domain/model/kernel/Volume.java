package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;

import java.util.List;

/**
 * Value Object, представляющий количество заказов.
 *
 * <p>
 * Используется как для количества заказов в назначении, так и для максимально допустимого количества заказов у курьера.
 * Допустимый диапазон: от {@value MIN} до {@value MAX} включительно.
 */
@Getter
public final class Volume extends ValueObject<Volume> {

    public static final int MIN = 0;
    public static final int MAX = 10;

    private final int value;

    private Volume(int value) {
        this.value = value;
    }

    /**
     * Создаёт объект Volume с валидацией диапазона [{@value MIN}, {@value MAX}].
     *
     * @param value
     *            количество заказов; допустимые значения: {@value MIN}–{@value MAX}
     *
     * @return {@code Result.success} с объектом Volume, либо {@code Result.failure} если value выходит за допустимый
     *         диапазон
     */
    public static Result<Volume, Error> create(int value) {
        Error error = Guard.againstOutOfRange(value, MIN, MAX, "volume");
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Volume(value));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
