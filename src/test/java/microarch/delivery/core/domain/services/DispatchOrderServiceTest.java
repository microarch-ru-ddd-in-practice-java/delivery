package microarch.delivery.core.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;

import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_20;

class DispatchOrderServiceTest {

    private final DispatchOrderService service = new DispatchOrderServiceImpl();

    private static Order order() {
        return Order.create(UUID.randomUUID(), L_5_5, VOLUME_5).getValue();
    }

    private static Courier courier(int x, int y) {
        return Courier.create("Courier", Location.create(x, y).getValue()).getValue();
    }

    // --- null guards ---

    @Test
    void dispatch_withNullOrder_throwsNullPointerException() {
        assertThatThrownBy(() -> service.dispatch(null, List.of())).isInstanceOf(NullPointerException.class);
    }

    @Test
    void dispatch_withNullCouriers_throwsNullPointerException() {
        assertThatThrownBy(() -> service.dispatch(order(), null)).isInstanceOf(NullPointerException.class);
    }

    // --- no available couriers ---

    @Test
    void dispatch_withEmptyCourierList_fails() {
        assertThat(service.dispatch(order(), List.of()).isFailure()).isTrue();
    }

    @Test
    void dispatch_whenAllCouriersFull_fails() {
        var order = order();
        var full = courier(5, 5);
        full.addAssignment(UUID.randomUUID(), VOLUME_20, L_5_5);

        assertThat(service.dispatch(order, List.of(full)).isFailure()).isTrue();
    }

    // --- successful dispatch ---

    @Test
    void dispatch_whenSingleAvailableCourier_succeeds() {
        assertThat(service.dispatch(order(), List.of(courier(5, 5))).isSuccess()).isTrue();
    }

    @Test
    void dispatch_whenSingleAvailableCourier_returnsThatCourier() {
        var courier = courier(5, 5);
        var result = service.dispatch(order(), List.of(courier));
        assertThat(result.getValue()).isEqualTo(courier);
    }

    @Test
    void dispatch_whenSingleAvailableCourier_orderBecomesAssigned() {
        var order = order();
        service.dispatch(order, List.of(courier(5, 5)));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void dispatch_whenSingleAvailableCourier_courierReceivesAssignment() {
        var courier = courier(5, 5);
        service.dispatch(order(), List.of(courier));
        assertThat(courier.getAssignments()).hasSize(1);
    }

    // --- closest courier wins ---

    @Test
    void dispatch_withMultipleCouriers_choosesClosest() {
        var close = courier(5, 4); // distance to (5,5) = 1
        var far = courier(1, 1); // distance to (5,5) = 8

        var result = service.dispatch(order(), List.of(far, close));

        assertThat(result.getValue()).isEqualTo(close);
    }

    @Test
    void dispatch_whenOneFullOneAvailable_choosesAvailable() {
        var full = courier(5, 5);
        full.addAssignment(UUID.randomUUID(), VOLUME_20, L_5_5);
        var available = courier(1, 1);

        var result = service.dispatch(order(), List.of(full, available));

        assertThat(result.getValue()).isEqualTo(available);
    }
}
