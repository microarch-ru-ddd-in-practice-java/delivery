package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;

/**
 * Порт входящей операции перемещения курьера.
 */
public interface MoveCourierCommandHandler {
    UnitResult<Error> handle(MoveCourierCommand command);
}
