package microarch.delivery.core.application.commands;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.out.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CreateOrderCommandHandlerImpl handler;

    @Test
    void handle_withValidCommand_savesOrderAndReturnsSuccess() {
        var command = CreateOrderCommand.create(UUID.randomUUID(), 5).getValueOrThrow();
        when(orderRepository.findById(command.getOrderId())).thenReturn(Optional.empty());

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void handle_whenOrderAlreadyExists_doesNotSaveAndReturnsSuccess() {
        var orderId = UUID.randomUUID();
        var command = CreateOrderCommand.create(orderId, 5).getValueOrThrow();
        var existingOrder = Order
                .create(orderId, Location.create(1, 1).getValueOrThrow(), Volume.create(5).getValueOrThrow())
                .getValueOrThrow();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_withNullOrderId_returnsFailure() {
        var result = CreateOrderCommand.create(null, 5);

        assertThat(result.isFailure()).isTrue();
    }
}
