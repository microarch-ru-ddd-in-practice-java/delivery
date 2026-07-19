package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.out.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * Обработчик команды создания заказа.
 *
 * <p>
 * Если заказ с таким идентификатором уже существует — возвращает успех без повторного создания (идемпотентность).
 * Местоположение доставки назначается случайным образом в допустимом диапазоне координат.
 */
@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private static final Random RANDOM = new Random();

    private final OrderRepository orderRepository;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public UnitResult<Error> handle(CreateOrderCommand command) {
        if (orderRepository.findById(command.getOrderId()).isPresent())
            return UnitResult.success();

        var volumeResult = Volume.create(command.getVolume());
        if (volumeResult.isFailure())
            return UnitResult.failure(volumeResult.getError());

        var location = randomLocation();
        if (location.isFailure())
            return UnitResult.failure(location.getError());

        var orderResult = Order.create(command.getOrderId(), location.getValue(), volumeResult.getValue());
        if (orderResult.isFailure())
            return UnitResult.failure(orderResult.getError());

        orderRepository.save(orderResult.getValue());
        return UnitResult.success();
    }

    private libs.errs.Result<Location, Error> randomLocation() {
        int x = Location.getMIN() + RANDOM.nextInt(Location.getMAX() - Location.getMIN() + 1);
        int y = Location.getMIN() + RANDOM.nextInt(Location.getMAX() - Location.getMIN() + 1);
        return Location.create(x, y);
    }
}
