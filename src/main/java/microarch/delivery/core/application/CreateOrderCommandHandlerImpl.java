package microarch.delivery.core.application;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.in.CreateOrderCommandHandler;
import microarch.delivery.core.ports.out.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация Use Case создания заказа.
 *
 * <p>
 * Создаёт заказ при получении события «Корзина оформлена» из Kafka. Id корзины используется как Id заказа.
 */
@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateOrderCommand command) {
        var locationResult = Location.create(command.locationX(), command.locationY());
        if (locationResult.isFailure()) {
            return UnitResult.failure(locationResult.getError());
        }

        var volumeResult = Volume.create(command.volume());
        if (volumeResult.isFailure()) {
            return UnitResult.failure(volumeResult.getError());
        }

        var orderResult = Order.create(command.basketId(), locationResult.getValue(), volumeResult.getValue());
        if (orderResult.isFailure()) {
            return UnitResult.failure(orderResult.getError());
        }

        orderRepository.save(orderResult.getValue());
        return UnitResult.success();
    }
}
