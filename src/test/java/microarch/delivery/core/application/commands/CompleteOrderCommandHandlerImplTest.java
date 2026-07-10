package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOrderCommandHandlerImplTest {

    @Test
    void completesCourierAssignmentAndOrderAndSavesChanges() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CourierRepository courierRepository = mock(CourierRepository.class);
        Order order = Order.create(
                UUID.randomUUID(),
                new Location(1, 1),
                Volume.create(2).getValue()
        ).getValue();
        Courier courier = Courier.create("Ivan", new Location(1, 1)).getValue();
        courier.takeOrder(order);
        order.assign();
        when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
        when(orderRepository.getById(order.getId())).thenReturn(Optional.of(order));
        CompleteOrderCommandHandler handler = new CompleteOrderCommandHandlerImpl(courierRepository, orderRepository);

        UnitResult<Error> result = handler.handle(new CompleteOrderCommand(courier.getId(), order.getId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(courier.getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        verify(orderRepository).update(order);
        verify(courierRepository).update(courier);
    }
}
