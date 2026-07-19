package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static microarch.delivery.testfixtures.TestLocations.L_1_1;
import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNotCompletedOrdersQueryHandlerTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private GetNotCompletedOrdersQueryHandlerImpl handler;

    @SuppressWarnings("unchecked")
    private TypedQuery<Order> mockQuery(List<Order> orders) {
        TypedQuery<Order> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Order.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(orders);
        return query;
    }

    @Test
    void handle_returnsOnlyNotCompletedOrders() {
        var order1 = Order.create(UUID.randomUUID(), L_1_1, VOLUME_5).getValueOrThrow();
        var order2 = Order.create(UUID.randomUUID(), L_5_5, VOLUME_5).getValueOrThrow();
        mockQuery(List.of(order1, order2));

        var result = handler.handle();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).hasSize(2);
    }

    @Test
    void handle_whenNoOrders_returnsEmptyList() {
        mockQuery(List.of());

        var result = handler.handle();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEmpty();
    }

    @Test
    void handle_dtoContainsCorrectIdAndLocation() {
        var orderId = UUID.randomUUID();
        var order = Order.create(orderId, L_1_1, VOLUME_5).getValueOrThrow();
        mockQuery(List.of(order));

        var result = handler.handle();

        var dto = result.getValue().getFirst();
        assertThat(dto.id()).isEqualTo(orderId);
        assertThat(dto.location()).isEqualTo(L_1_1);
    }

    @Test
    void handle_queryFiltersOnCompletedStatus() {
        TypedQuery<Order> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Order.class))).thenReturn(query);
        when(query.setParameter(eq("status"), eq(OrderStatus.COMPLETED))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        handler.handle();

        org.mockito.Mockito.verify(query).setParameter("status", OrderStatus.COMPLETED);
    }
}
