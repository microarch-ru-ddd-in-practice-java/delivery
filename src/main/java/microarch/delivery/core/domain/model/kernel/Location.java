package microarch.delivery.core.domain.model.kernel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Location extends ValueObject<Location> {
    private static final int MIN_COORDINATE = 1;
    private static final int MAX_COORDINATE = 10;

    @Column(name = "location_x", nullable = false)
    private final int x;
    @Column(name = "location_y", nullable = false)
    private final int y;

    public static Result<Location, Error> create(int x, int y) {
        var error = Guard.combine(
                Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "x"),
                Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "y"));
        if (error != null)
            return Result.failure(error);

        return Result.success(new Location(x, y));
    }

    public int distanceTo(Location other) {
        Objects.requireNonNull(other, "other location is null");

        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    public boolean isWithinOneStepOf(Location other) {
        return distanceTo(other) <= 1;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(this.x, this.y);
    }
}
