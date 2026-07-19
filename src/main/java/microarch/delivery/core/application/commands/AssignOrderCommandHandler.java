package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;

/**
 * Порт входящей операции назначения заказа на курьера.
 */
public interface AssignOrderCommandHandler {
    UnitResult<Error> handle(AssignOrderCommand command);
}
