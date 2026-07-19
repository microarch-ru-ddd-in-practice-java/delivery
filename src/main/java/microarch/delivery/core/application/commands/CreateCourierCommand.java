package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Команда создания курьера.
 *
 * <p>
 * Принимает только имя курьера. Начальное местоположение назначается обработчиком случайным образом.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateCourierCommand {

    private final String name;

    /**
     * Создаёт команду с валидацией имени.
     *
     * @param name имя курьера; обязательно, не должно быть пустым
     * @return {@code Result.success} с командой, либо {@code Result.failure} если имя пустое или null
     */
    public static Result<CreateCourierCommand, Error> create(String name) {
        var err = Guard.againstNullOrEmpty(name, "name");
        if (err != null)
            return Result.failure(err);

        return Result.success(new CreateCourierCommand(name));
    }
}
