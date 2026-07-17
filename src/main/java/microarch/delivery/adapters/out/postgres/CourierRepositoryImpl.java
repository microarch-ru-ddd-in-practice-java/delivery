package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.out.CourierRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Адаптер репозитория курьеров, реализующий порт {@link CourierRepository} через Spring Data JPA.
 */
@Repository
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository jpa;

    public CourierRepositoryImpl(CourierJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void save(Courier courier) {
        jpa.save(courier);
    }

    @Override
    @Transactional
    public void update(Courier courier) {
        jpa.save(courier);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Courier> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Courier> findAll() {
        return jpa.findAll();
    }
}
