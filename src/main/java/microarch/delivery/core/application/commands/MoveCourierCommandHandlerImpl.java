package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.out.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Обработчик команды перемещения курьера.
 *
 * <p>
 * Перемещает курьера на 1 шаг в сторону указанной координаты. Расстояние до новой точки
 * не должно превышать 1 (манхэттенское).
 */
@Service
public class MoveCourierCommandHandlerImpl implements MoveCourierCommandHandler {

    private final CourierRepository courierRepository;

    public MoveCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Transactional
    @Override
    public UnitResult<Error> handle(MoveCourierCommand command) {
        var courierOpt = courierRepository.findById(command.getCourierId());
        if (courierOpt.isEmpty())
            return UnitResult.failure(GeneralErrors.notFound("Courier", command.getCourierId()));

        var courier = courierOpt.get();
        var moveResult = courier.move(command.getLocation());
        if (moveResult.isFailure())
            return UnitResult.failure(moveResult.getError());

        courierRepository.update(courier);
        return UnitResult.success();
    }
}
