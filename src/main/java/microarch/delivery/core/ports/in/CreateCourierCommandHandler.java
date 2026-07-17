package microarch.delivery.core.ports.in;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.application.CreateCourierCommand;

/**
 * Входящий порт создания курьера.
 */
public interface CreateCourierCommandHandler {

    /**
     * Создаёт курьера с указанным именем и случайным начальным местоположением.
     *
     * @param command команда с именем курьера
     * @return {@code UnitResult.success} если курьер создан, {@code UnitResult.failure} если имя невалидно
     */
    UnitResult<Error> handle(CreateCourierCommand command);
}
