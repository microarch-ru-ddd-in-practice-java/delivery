package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderCommandHandlerImpl.class);

    private final OrderRepository orderRepository;
    private final GeoClient geoClient;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository, GeoClient geoClient) {
        this.orderRepository = orderRepository;
        this.geoClient = geoClient;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateOrderCommand command) {
        if (command == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("command"));
        }

        Error validationError = command.validate();
        if (validationError != null) {
            return UnitResult.failure(validationError);
        }

        Result<Volume, Error> volumeResult = Volume.create(command.volume());

        if (volumeResult.isFailure()) {
            return UnitResult.failure(volumeResult.getError());
        }

        Result<Location, Error> locationResult = geoClient.getGeolocation(command.street());

        if (locationResult == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("location"));
        }

        if (locationResult.isFailure()) {
            return UnitResult.failure(locationResult.getError());
        }

        Result<Order, Error> orderResult = Order.create(
                command.orderId(),
                locationResult.getValue(),
                volumeResult.getValue()
        );

        if (orderResult.isFailure()) {
            return UnitResult.failure(orderResult.getError());
        }

        orderRepository.add(orderResult.getValue());
        log.info("Order created and added to repository: orderId={}", command.orderId());
        return UnitResult.success();
    }
}
