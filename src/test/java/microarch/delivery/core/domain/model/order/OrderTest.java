package microarch.delivery.core.domain.model.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static microarch.delivery.testfixtures.TestLocations.L_3_4;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_5;

class OrderTest {

    private static final UUID ID = UUID.randomUUID();

    private static Order validOrder() {
        return Order.create(ID, L_3_4, VOLUME_5).getValue();
    }

    // --- creation: valid ---

    @Test
    void create_withValidParams_succeeds() {
        assertThat(Order.create(ID, L_3_4, VOLUME_5).isSuccess()).isTrue();
    }

    @Test
    void create_withValidParams_statusIsCreated() {
        assertThat(validOrder().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    // --- creation: invalid ---

    @Test
    void create_withNullId_fails() {
        assertThat(Order.create(null, L_3_4, VOLUME_5).isFailure()).isTrue();
    }

    @Test
    void create_withNullLocation_fails() {
        assertThat(Order.create(ID, null, VOLUME_5).isFailure()).isTrue();
    }

    @Test
    void create_withNullVolume_fails() {
        assertThat(Order.create(ID, L_3_4, null).isFailure()).isTrue();
    }

    // --- assign ---

    @Test
    void assign_whenCreated_succeeds() {
        assertThat(validOrder().assign().isSuccess()).isTrue();
    }

    @Test
    void assign_whenCreated_statusBecomesAssigned() {
        var order = validOrder();
        order.assign();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = { "ASSIGNED", "COMPLETED" })
    void assign_whenNotCreated_fails(OrderStatus status) {
        var order = validOrder();
        if (status == OrderStatus.ASSIGNED)
            order.assign();
        if (status == OrderStatus.COMPLETED) {
            order.assign();
            order.complete();
        }
        assertThat(order.assign().isFailure()).isTrue();
    }

    // --- complete ---

    @Test
    void complete_whenAssigned_succeeds() {
        var order = validOrder();
        order.assign();
        assertThat(order.complete().isSuccess()).isTrue();
    }

    @Test
    void complete_whenAssigned_statusBecomesCompleted() {
        var order = validOrder();
        order.assign();
        order.complete();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = { "CREATED", "COMPLETED" })
    void complete_whenNotAssigned_fails(OrderStatus status) {
        var order = validOrder();
        if (status == OrderStatus.COMPLETED) {
            order.assign();
            order.complete();
        }
        assertThat(order.complete().isFailure()).isTrue();
    }
}
