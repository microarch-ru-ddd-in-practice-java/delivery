package microarch.delivery.core.domain.model.courier;

import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

    @Test
    void createInitializesCourierWithMaxVolumeAndEmptyAssignments() {
        Location location = new Location(1, 1);

        Result<Courier, Error> result = Courier.create("Ivan", location);

        assertThat(result.isSuccess()).isTrue();
        Courier courier = result.getValue();
        assertThat(courier.getId()).isNotNull();
        assertThat(courier.getName()).isEqualTo("Ivan");
        assertThat(courier.getLocation()).isEqualTo(location);
        assertThat(courier.getMaxVolume()).isEqualTo(Volume.create(Courier.MAX_VOLUME).getValue());
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void takeOrderFailsWhenVolumeLimitWouldBeExceeded() {
        Courier courier = Courier.create("Ivan", new Location(1, 1)).getValue();

        courier.takeOrder(order(Volume.create(15).getValue(), new Location(2, 2)));
        UnitResult<Error> result = courier.takeOrder(
                order(Volume.create(6).getValue(), new Location(3, 3))
        );

        assertThat(result.isFailure()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void completeAssignmentCompletesOnlyWhenCourierIsCloseEnough() {
        Courier courier = Courier.create("Ivan", new Location(1, 1)).getValue();
        Order closeOrder = order(Volume.create(1).getValue(), new Location(1, 2));
        Order farOrder = order(Volume.create(1).getValue(), new Location(5, 5));

        courier.takeOrder(closeOrder);
        courier.takeOrder(farOrder);

        UnitResult<Error> closeResult = courier.completeAssignment(
                courier.getAssignments().getFirst().getId()
        );
        UnitResult<Error> farResult = courier.completeAssignment(
                courier.getAssignments().get(1).getId()
        );

        assertThat(closeResult.isSuccess()).isTrue();
        assertThat(farResult.isFailure()).isTrue();
        assertThat(courier.getAssignments().getFirst().getStatus())
                .isEqualTo(AssignmentStatus.COMPLETED);
        assertThat(courier.getAssignments().get(1).getStatus())
                .isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void changeLocationUpdatesCourierLocation() {
        Courier courier = Courier.create("Ivan", new Location(1, 1)).getValue();
        Location newLocation = new Location(10, 10);

        courier.changeLocation(newLocation);

        assertThat(courier.getLocation()).isEqualTo(newLocation);
    }

    private static Order order(Volume volume, Location location) {
        return Order.create(UUID.randomUUID(), location, volume).getValue();
    }
}
