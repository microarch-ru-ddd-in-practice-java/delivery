package microarch.delivery.adapters.in.kafka;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import queues.basket.events.BasketEventsProto;

import java.util.UUID;

@Component
public class BasketConfirmedIntegrationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BasketConfirmedIntegrationEventConsumer.class);

    private final CreateOrderCommandHandler createOrderCommandHandler;

    public BasketConfirmedIntegrationEventConsumer(CreateOrderCommandHandler createOrderCommandHandler) {
        this.createOrderCommandHandler = createOrderCommandHandler;
    }

    @KafkaListener(
            topics = "${app.kafka.basket-events-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(byte[] message) {
        log.debug("Got basket confirmed integration event payload: {} bytes", message.length);
        try {
            BasketEventsProto.BasketConfirmedIntegrationEvent event =
                    BasketEventsProto.BasketConfirmedIntegrationEvent.parseFrom(message);

            UnitResult<Error> result = createOrderCommandHandler.handle(new CreateOrderCommand(
                    UUID.fromString(event.getBasketId()),
                    event.getAddress().getCountry(),
                    event.getAddress().getCity(),
                    event.getAddress().getStreet(),
                    event.getAddress().getHouse(),
                    event.getAddress().getApartment(),
                    event.getVolume()
            ));

            if (result.isFailure()) {
                log.error("Failed to create order from basket event: {}", result.getError().serialize());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    };
}
