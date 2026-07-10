package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresCourierRepository implements CourierRepository {

    private final CourierJpaRepository repository;

    public PostgresCourierRepository(CourierJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void add(Courier courier) {
        save(courier);
    }

    @Override
    public void update(Courier courier) {
        save(courier);
    }

    @Override
    public Optional<Courier> getById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");

        return repository.findById(id).map(CourierJpaEntity::toDomain);
    }

    @Override
    public List<Courier> getAll() {
        return repository.findAll().stream()
                .map(CourierJpaEntity::toDomain)
                .toList();
    }

    private void save(Courier courier) {
        Objects.requireNonNull(courier, "courier must not be null");

        repository.save(CourierJpaEntity.fromDomain(courier));
    }
}
