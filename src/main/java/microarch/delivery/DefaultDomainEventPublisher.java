package microarch.delivery;

import libs.ddd.Aggregate;
import libs.ddd.DomainEvent;
import libs.ddd.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DefaultDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher publisher;

    public DefaultDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(Iterable<? extends Aggregate<?>> aggregates) {
        for (Aggregate<?> aggregate : aggregates) {
            for (DomainEvent event : aggregate.getDomainEvents()) {
                publisher.publishEvent(event);
            }
        }
    }
}