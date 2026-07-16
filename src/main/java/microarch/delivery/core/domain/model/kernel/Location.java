package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;

import java.util.List;
import java.util.Objects;

/**
 * Value Object, представляющий координату на доске.
 *
 * <p>
 * Координата задаётся двумя осями: X (горизонталь) и Y (вертикаль). Допустимый диапазон для каждой оси — от 1 до 10
 * включительно. Объект неизменяем: после создания его состояние не может быть изменено. Два объекта Location равны,
 * если равны их X и Y.
 */
public final class Location extends ValueObject<Location> {

    private static final int MIN = 1;
    private static final int MAX = 10;

    private final int x;
    private final int y;

    private Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Создаёт объект Location с валидацией координат.
     *
     * @param x
     *            горизонтальная координата, допустимые значения: 1–10
     * @param y
     *            вертикальная координата, допустимые значения: 1–10
     *
     * @return {@code Result.success} с объектом Location, либо {@code Result.failure} если x или y выходят за
     *         допустимый диапазон
     */
    public static Result<Location, Error> create(int x, int y) {
        Error error = Guard.combine(Guard.againstOutOfRange(x, MIN, MAX, "x"),
                Guard.againstOutOfRange(y, MIN, MAX, "y"));
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Location(x, y));
    }

    /**
     * Вычисляет манхэттенское расстояние до другой координаты.
     *
     * <p>
     * Расстояние — это суммарное количество шагов по X и Y, которое необходимо сделать курьеру, двигаясь только по
     * вертикали и горизонтали. Формула: {@code |this.x - other.x| + |this.y - other.y|}.
     *
     * @param other
     *            целевая координата; не должна быть {@code null}
     *
     * @return количество шагов до целевой координаты
     *
     * @throws NullPointerException
     *             если {@code other} равен {@code null}
     */
    public int distanceTo(Location other) {
        Objects.requireNonNull(other, "Location must not be null");
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * Возвращает компоненты для сравнения двух объектов Location.
     *
     * <p>
     * Два объекта Location равны, если равны их X и Y.
     */
    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
