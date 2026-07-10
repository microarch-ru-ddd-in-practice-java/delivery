package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import microarch.delivery.core.application.queries.dto.IncompleteOrderDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GetAllIncompleteOrdersQueryHandlerImpl implements GetAllIncompleteOrdersQueryHandler {

    private final EntityManager entityManager;

    public GetAllIncompleteOrdersQueryHandlerImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<IncompleteOrderDto> handle(GetAllIncompleteOrdersQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        List<Object[]> response = entityManager.createQuery("""
                        select orderEntity.id, orderEntity.locationX, orderEntity.locationY
                        from OrderJpaEntity orderEntity
                        where orderEntity.status in :statuses
                        """, Object[].class)
                .setParameter("statuses", List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED))
                .getResultList();

        return response.stream()
                .map(row -> new IncompleteOrderDto(
                        (UUID) row[0],
                        new LocationDto((Integer) row[1], (Integer) row[2])
                ))
                .toList();
    }
}
