package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {

    private static final Location INITIAL_LOCATION = new Location(
            Location.MIN_COORDINATE,
            Location.MIN_COORDINATE
    );

    private final CourierRepository courierRepository;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateCourierCommand command) {
        if (command == null) {
            return Result.failure(GeneralErrors.valueIsRequired("command"));
        }

        Error validationError = command.validate();
        if (validationError != null) {
            return Result.failure(validationError);
        }

        Result<Courier, Error> courierResult = Courier.create(command.name(), INITIAL_LOCATION);

        if (courierResult.isFailure()) {
            return Result.failure(courierResult.getError());
        }

        Courier courier = courierResult.getValue();
        courierRepository.add(courier);
        return Result.success(courier.getId());
    }
}
