package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import microarch.delivery.core.application.queries.dto.CourierDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAllCouriersQueryHandlerImplTest {

    @Test
    void returnsCourierDtosFromDatabaseRows() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Object[]> query = mockQuery(List.<Object[]>of(new Object[]{
                UUID.randomUUID(),
                "Ivan",
                3,
                4
        }));
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        GetAllCouriersQueryHandler handler = new GetAllCouriersQueryHandlerImpl(entityManager);

        List<CourierDto> result = handler.handle(new GetAllCouriersQuery());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Ivan");
        assertThat(result.getFirst().location().x()).isEqualTo(3);
        assertThat(result.getFirst().location().y()).isEqualTo(4);
    }

    private static TypedQuery<Object[]> mockQuery(List<Object[]> rows) {
        @SuppressWarnings("unchecked")
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(query.getResultList()).thenReturn(rows);
        return query;
    }
}
