package microarch.delivery.core.application;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.in.CreateCourierCommandHandler;
import microarch.delivery.core.ports.out.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Реализация Use Case создания курьера.
 *
 * <p>
 * Начальное местоположение курьера выбирается случайно в пределах доски (1–10 по каждой оси).
 */
@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {

    private final CourierRepository courierRepository;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateCourierCommand command) {
        int x = ThreadLocalRandom.current().nextInt(Location.getMIN(), Location.getMAX() + 1);
        int y = ThreadLocalRandom.current().nextInt(Location.getMIN(), Location.getMAX() + 1);

        var locationResult = Location.create(x, y);
        if (locationResult.isFailure()) {
            return UnitResult.failure(locationResult.getError());
        }

        var courierResult = Courier.create(command.name(), locationResult.getValue());
        if (courierResult.isFailure()) {
            return UnitResult.failure(courierResult.getError());
        }

        courierRepository.save(courierResult.getValue());
        return UnitResult.success();
    }
}
