package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.ports.out.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Обработчик запроса на получение всех курьеров.
 */
@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {

    private final CourierRepository courierRepository;

    public GetAllCouriersQueryHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Result<List<CourierDto>, Error> handle() {
        var couriers = courierRepository.findAll();
        var dtos = couriers.stream()
                .map(c -> new CourierDto(c.getId(), c.getName(), c.getLocation()))
                .toList();
        return Result.success(dtos);
    }
}
