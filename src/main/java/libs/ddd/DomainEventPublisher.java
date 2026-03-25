package libs.ddd;

public interface DomainEventPublisher {
    void publish(Iterable<? extends Aggregate<?>> aggregates);
}