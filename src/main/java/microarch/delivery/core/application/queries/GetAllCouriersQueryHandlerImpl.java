package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {

    private final EntityManager entityManager;

    public GetAllCouriersQueryHandlerImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<CourierDto> handle(GetAllCouriersQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        List<Object[]> response = entityManager.createQuery("""
                        select courier.id, courier.name, courier.locationX, courier.locationY
                        from CourierJpaEntity courier
                        """, Object[].class)
                .getResultList();

        return response.stream()
                .map(row -> new CourierDto(
                        (UUID) row[0],
                        (String) row[1],
                        new LocationDto((Integer) row[2], (Integer) row[3])
                ))
                .toList();
    }
}
