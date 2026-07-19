package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.out.CourierRepository;
import microarch.delivery.core.ports.out.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Обработчик команды завершения доставки.
 *
 * <p>
 * Завершает назначение у курьера и переводит заказ в статус {@code COMPLETED} в одной транзакции.
 */
@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var courierOpt = courierRepository.findById(command.getCourierId());
        if (courierOpt.isEmpty())
            return UnitResult.failure(GeneralErrors.notFound("Courier", command.getCourierId()));

        var orderOpt = orderRepository.findById(command.getOrderId());
        if (orderOpt.isEmpty())
            return UnitResult.failure(GeneralErrors.notFound("Order", command.getOrderId()));

        var courier = courierOpt.get();
        var order = orderOpt.get();

        var completeAssignmentResult = courier.completeAssignment(command.getOrderId());
        if (completeAssignmentResult.isFailure())
            return UnitResult.failure(completeAssignmentResult.getError());

        var completeOrderResult = order.complete();
        if (completeOrderResult.isFailure())
            return UnitResult.failure(completeOrderResult.getError());

        courierRepository.update(courier);
        orderRepository.update(order);
        return UnitResult.success();
    }
}
