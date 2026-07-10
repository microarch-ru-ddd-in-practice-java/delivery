package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import microarch.delivery.core.application.queries.dto.IncompleteOrderDto;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetAllIncompleteOrdersQueryHandlerImplTest {

    @Test
    void returnsCreatedAndAssignedOrderDtos() {
        EntityManager entityManager = mock(EntityManager.class);
        UUID orderId = UUID.randomUUID();
        TypedQuery<Object[]> query = mockQuery(List.<Object[]>of(new Object[]{
                orderId,
                5,
                6
        }));
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(eq("statuses"), eq(List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED))))
                .thenReturn(query);
        GetAllIncompleteOrdersQueryHandler handler = new GetAllIncompleteOrdersQueryHandlerImpl(entityManager);

        List<IncompleteOrderDto> result = handler.handle(new GetAllIncompleteOrdersQuery());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(orderId);
        assertThat(result.getFirst().location().x()).isEqualTo(5);
        assertThat(result.getFirst().location().y()).isEqualTo(6);
        verify(query).setParameter("statuses", List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED));
    }

    private static TypedQuery<Object[]> mockQuery(List<Object[]> rows) {
        @SuppressWarnings("unchecked")
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(query.getResultList()).thenReturn(rows);
        return query;
    }
}
