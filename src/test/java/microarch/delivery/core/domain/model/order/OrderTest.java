package microarch.delivery.core.domain.model.order;

import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void createInitializesOrderWithCreatedStatus() {
        UUID orderId = UUID.randomUUID();
        Location location = new Location(5, 7);
        Volume volume = Volume.create(3).getValue();

        Result<Order, Error> result = Order.create(orderId, location, volume);

        assertThat(result.isSuccess()).isTrue();
        Order order = result.getValue();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getLocation()).isEqualTo(location);
        assertThat(order.getVolume()).isEqualTo(volume);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void assignChangesStatusOnlyFromCreated() {
        Order order = order(Volume.create(1).getValue(), new Location(1, 1));

        UnitResult<Error> firstAssign = order.assign();
        UnitResult<Error> secondAssign = order.assign();

        assertThat(firstAssign.isSuccess()).isTrue();
        assertThat(secondAssign.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void completeChangesStatusOnlyFromAssigned() {
        Order createdOrder = order(Volume.create(1).getValue(), new Location(1, 1));
        Order assignedOrder = order(Volume.create(1).getValue(), new Location(2, 2));

        UnitResult<Error> completeCreated = createdOrder.complete();
        assignedOrder.assign();
        UnitResult<Error> completeAssigned = assignedOrder.complete();

        assertThat(completeCreated.isFailure()).isTrue();
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(completeAssigned.isSuccess()).isTrue();
        assertThat(assignedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    private static Order order(Volume volume, Location location) {
        return Order.create(UUID.randomUUID(), location, volume).getValue();
    }
}
