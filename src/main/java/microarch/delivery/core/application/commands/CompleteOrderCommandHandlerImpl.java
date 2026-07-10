package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        if (command == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("command"));
        }

        Error validationError = command.validate();
        if (validationError != null) {
            return UnitResult.failure(validationError);
        }

        Courier courier = courierRepository.getById(command.courierId())
                .orElse(null);

        if (courier == null) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.courierId()));
        }

        Order order = orderRepository.getById(command.orderId())
                .orElse(null);

        if (order == null) {
            return UnitResult.failure(GeneralErrors.notFound("order", command.orderId()));
        }

        UnitResult<Error> completeAssignmentResult = courier.completeAssignmentForOrder(order.getId());

        if (completeAssignmentResult.isFailure()) {
            return completeAssignmentResult;
        }

        UnitResult<Error> completeOrderResult = order.complete();

        if (completeOrderResult.isFailure()) {
            return completeOrderResult;
        }

        courierRepository.update(courier);
        orderRepository.update(order);
        return UnitResult.success();
    }
}
