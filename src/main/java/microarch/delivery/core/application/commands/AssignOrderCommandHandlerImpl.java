package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.services.OrderDistributionDomainService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderDistributionDomainService orderDistributionDomainService;

    public AssignOrderCommandHandlerImpl(
            OrderRepository orderRepository,
            CourierRepository courierRepository,
            OrderDistributionDomainService orderDistributionDomainService
    ) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.orderDistributionDomainService = orderDistributionDomainService;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {
        if (command == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("command"));
        }

        Order order = orderRepository.getAnyCreated()
                .orElse(null);

        if (order == null) {
            return UnitResult.failure(GeneralErrors.notFound("order", "created"));
        }

        List<Courier> couriers = courierRepository.getAll();
        Result<Courier, Error> dispatchResult = orderDistributionDomainService.distributeOrder(order, couriers);

        if (dispatchResult.isFailure()) {
            return UnitResult.failure(dispatchResult.getError());
        }

        courierRepository.update(dispatchResult.getValue());
        orderRepository.update(order);
        return UnitResult.success();
    }
}
