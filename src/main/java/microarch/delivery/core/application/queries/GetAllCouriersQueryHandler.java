package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.CourierDto;

import java.util.List;

/**
 * Порт входящей операции получения всех курьеров.
 */
public interface GetAllCouriersQueryHandler {
    Result<List<CourierDto>, Error> handle();
}
