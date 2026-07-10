package microarch.delivery.core.domain.model.kernel;

import java.util.List;
import libs.ddd.ValueObject;
import libs.errs.DomainInvariantException;
import libs.errs.GeneralErrors;
import lombok.Getter;

@Getter
public final class Location extends ValueObject<Location> {
    public static final int MIN_COORDINATE = 1;
    public static final int MAX_COORDINATE = 10;

    private final int x;
    private final int y;

    public Location(int x, int y) {
        validateCoordinate(x, "x");
        validateCoordinate(y, "y");

        this.x = x;
        this.y = y;
    }

    public int distanceTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("Location must not be null");
        }

        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }

    private static void validateCoordinate(int value, String name) {
        if (value < MIN_COORDINATE || value > MAX_COORDINATE) {
            throw new DomainInvariantException(
                    GeneralErrors.valueIsOutOfRange(name, value, MIN_COORDINATE, MAX_COORDINATE));
        }
    }
}
