package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;

import java.util.UUID;

/**
 * Команда перемещения курьера на одну клетку.
 *
 * <p>
 * Курьер перемещается на 1 шаг в сторону целевой координаты.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveCourierCommand {

    private final UUID courierId;
    private final Location location;

    /**
     * Создаёт команду с валидацией параметров.
     *
     * @param courierId идентификатор курьера; обязателен
     * @param location  целевое местоположение; обязательно
     * @return {@code Result.success} с командой, либо {@code Result.failure} если параметр невалиден
     */
    public static Result<MoveCourierCommand, Error> create(UUID courierId, Location location) {
        Error err = Guard.combine(
                Guard.againstNullOrEmpty(courierId, "courierId"),
                location == null ? GeneralErrors.valueIsRequired("location") : null);
        if (err != null)
            return Result.failure(err);

        return Result.success(new MoveCourierCommand(courierId, location));
    }
}
