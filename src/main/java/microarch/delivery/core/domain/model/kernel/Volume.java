package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;

import java.util.List;

/**
 * Value Object, представляющий объём заказа.
 *
 * <p>
 * Объём не может быть отрицательным (не менее {@value MIN}). Верхнего ограничения нет — максимально допустимый объём
 * определяется каждым курьером индивидуально и хранится в агрегате Courier.
 */
public final class Volume extends ValueObject<Volume> {

    public static final int MIN = 0;

    private final int value;

    private Volume(int value) {
        this.value = value;
    }

    /**
     * Создаёт объект Volume с валидацией нижней границы.
     *
     * @param value объём заказа; должен быть не менее {@value MIN} (не отрицательным)
     * @return {@code Result.success} с объектом Volume, либо {@code Result.failure} если value отрицательный
     */
    public static Result<Volume, Error> create(int value) {
        Error error = Guard.againstLessThan(value, MIN, "volume");
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Volume(value));
    }

    /**
     * Возвращает числовое значение объёма.
     *
     * @return значение объёма
     */
    public int getValue() {
        return value;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
