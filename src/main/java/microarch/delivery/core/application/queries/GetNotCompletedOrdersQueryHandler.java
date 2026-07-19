package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.OrderDto;

import java.util.List;

/**
 * Порт входящей операции получения незавершённых заказов.
 */
public interface GetNotCompletedOrdersQueryHandler {
    Result<List<OrderDto>, Error> handle();
}
