package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDistributionDomainServiceImplTest {

    private final OrderDistributionDomainService service = new OrderDistributionDomainServiceImpl();

    @Test
    void distributeOrderAssignsClosestAvailableCourierAndKeepsStateConsistent() {
        Courier fullCourier = fullCourier("Full");
        Courier farCourier = courier("Far", new Location(10, 10));
        Courier closestAvailableCourier = courier("Closest", new Location(2, 2));
        Order order = order(Volume.create(3).getValue(), new Location(3, 2));

        Result<Courier, Error> result = service.distributeOrder(
                order,
                List.of(fullCourier, farCourier, closestAvailableCourier)
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(closestAvailableCourier);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(closestAvailableCourier.getAssignments()).hasSize(1);
        assertThat(closestAvailableCourier.getAssignments().getFirst().getOrderId()).isEqualTo(order.getId());
        assertThat(fullCourier.getAssignments()).hasSize(1);
        assertThat(farCourier.getAssignments()).isEmpty();
    }

    @Test
    void distributeOrderReturnsBusinessErrorWhenNoAvailableCouriers() {
        Order orderWithoutCouriers = order(Volume.create(1).getValue(), new Location(1, 1));

        Result<Courier, Error> noCouriersResult = service.distributeOrder(orderWithoutCouriers, List.of());
        Courier firstCourier = fullCourier("First");
        Courier secondCourier = fullCourier("Second");
        Order orderForFullCouriers = order(Volume.create(1).getValue(), new Location(1, 1));

        Result<Courier, Error> allFullResult = service.distributeOrder(
                orderForFullCouriers,
                List.of(firstCourier, secondCourier)
        );

        assertThat(noCouriersResult.isFailure()).isTrue();
        assertThat(noCouriersResult.getError().getCode()).isEqualTo("order.dispatch.failed");
        assertThat(orderWithoutCouriers.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(allFullResult.isFailure()).isTrue();
        assertThat(allFullResult.getError().getCode()).isEqualTo("order.dispatch.failed");
        assertThat(orderForFullCouriers.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(firstCourier.getAssignments()).hasSize(1);
        assertThat(secondCourier.getAssignments()).hasSize(1);
    }

    private static Courier fullCourier(String name) {
        Courier courier = courier(name, new Location(1, 1));
        courier.takeOrder(order(Volume.create(Courier.MAX_VOLUME).getValue(), new Location(1, 1)));
        return courier;
    }

    private static Courier courier(String name, Location location) {
        return Courier.create(name, location).getValue();
    }

    private static Order order(Volume volume, Location location) {
        return Order.create(UUID.randomUUID(), location, volume).getValue();
    }
}
