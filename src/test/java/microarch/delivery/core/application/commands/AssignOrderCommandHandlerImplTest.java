package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.services.OrderDistributionDomainService;
import microarch.delivery.core.domain.services.OrderDistributionDomainServiceImpl;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignOrderCommandHandlerImplTest {

    @Test
    void assignsCreatedOrderToCourierAndSavesChanges() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CourierRepository courierRepository = mock(CourierRepository.class);
        Order order = Order.create(
                UUID.randomUUID(),
                new Location(2, 2),
                Volume.create(2).getValue()
        ).getValue();
        Courier courier = Courier.create("Ivan", new Location(2, 2)).getValue();
        when(orderRepository.getAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.getAll()).thenReturn(List.of(courier));
        OrderDistributionDomainService orderDistributionDomainService = new OrderDistributionDomainServiceImpl();
        AssignOrderCommandHandler handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, orderDistributionDomainService);

        UnitResult<Error> result = handler.handle(new AssignOrderCommand());

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(courier.getAssignments()).hasSize(1);
        assertThat(courier.getAssignments().getFirst().getOrderId()).isEqualTo(order.getId());
        verify(orderRepository).update(order);
        verify(courierRepository).update(courier);
    }
}
