package microarch.delivery.core.ports.in;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.application.CreateOrderCommand;

/**
 * Входящий порт создания заказа.
 */
public interface CreateOrderCommandHandler {

    /**
     * Создаёт заказ на основе данных оформленной корзины.
     *
     * @param command команда с Id корзины, координатами и объёмом
     * @return {@code UnitResult.success} если заказ создан, {@code UnitResult.failure} если данные невалидны
     */
    UnitResult<Error> handle(CreateOrderCommand command);
}
