package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.out.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

/**
 * Обработчик команды создания курьера.
 *
 * <p>
 * Создаёт курьера с указанным именем. Начальное местоположение назначается случайным образом в допустимом диапазоне
 * координат.
 */
@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {

    private static final Random RANDOM = new Random();

    private final CourierRepository courierRepository;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Transactional
    @Override
    public Result<UUID, Error> handle(CreateCourierCommand command) {
        var locationResult = randomLocation();
        if (locationResult.isFailure())
            return Result.failure(locationResult.getError());

        var courierResult = Courier.create(command.getName(), locationResult.getValue());
        if (courierResult.isFailure())
            return Result.failure(courierResult.getError());

        var courier = courierResult.getValue();
        courierRepository.save(courier);
        return Result.success(courier.getId());
    }

    private Result<Location, Error> randomLocation() {
        int x = Location.getMIN() + RANDOM.nextInt(Location.getMAX() - Location.getMIN() + 1);
        int y = Location.getMIN() + RANDOM.nextInt(Location.getMAX() - Location.getMIN() + 1);
        return Location.create(x, y);
    }
}
