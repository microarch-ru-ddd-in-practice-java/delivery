package microarch.delivery.core.application.commands;

import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.services.DispatchOrderService;
import microarch.delivery.core.ports.out.CourierRepository;
import microarch.delivery.core.ports.out.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static microarch.delivery.testfixtures.TestLocations.L_1_1;
import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private DispatchOrderService dispatchOrderService;

    @InjectMocks
    private AssignOrderCommandHandlerImpl handler;

    @Test
    void handle_whenNoCreatedOrder_returnsSuccessWithoutDispatching() {
        when(orderRepository.findFirstCreated()).thenReturn(Optional.empty());

        var result = handler.handle(AssignOrderCommand.create());

        assertThat(result.isSuccess()).isTrue();
        verify(dispatchOrderService, never()).dispatch(any(), anyList());
    }

    @Test
    void handle_withOrderAndCourier_dispatchesAndUpdatesBoth() {
        var order = Order.create(UUID.randomUUID(), L_1_1, VOLUME_5).getValueOrThrow();
        var courier = Courier.create("Иван", L_5_5).getValueOrThrow();
        when(orderRepository.findFirstCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAll()).thenReturn(List.of(courier));
        when(dispatchOrderService.dispatch(order, List.of(courier))).thenReturn(Result.success(courier));

        var result = handler.handle(AssignOrderCommand.create());

        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository).update(order);
        verify(courierRepository).update(courier);
    }

    @Test
    void handle_whenDispatchFails_returnsFailure() {
        var order = Order.create(UUID.randomUUID(), L_1_1, VOLUME_5).getValueOrThrow();
        when(orderRepository.findFirstCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAll()).thenReturn(List.of());
        when(dispatchOrderService.dispatch(any(), anyList()))
                .thenReturn(Result.failure(DispatchOrderService.Errors.noCourierAvailable()));

        var result = handler.handle(AssignOrderCommand.create());

        assertThat(result.isFailure()).isTrue();
        verify(orderRepository, never()).update(any());
    }
}
