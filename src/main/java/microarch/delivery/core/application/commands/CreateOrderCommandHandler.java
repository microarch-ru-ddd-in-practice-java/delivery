package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;

/**
 * Порт входящей операции создания заказа.
 */
public interface CreateOrderCommandHandler {
    UnitResult<Error> handle(CreateOrderCommand command);
}
