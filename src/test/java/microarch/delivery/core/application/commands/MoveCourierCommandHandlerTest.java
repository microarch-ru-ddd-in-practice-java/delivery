package microarch.delivery.core.application.commands;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.out.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestLocations.L_6_5;
import static microarch.delivery.testfixtures.TestLocations.L_10_10;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveCourierCommandHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private MoveCourierCommandHandlerImpl handler;

    @Test
    void handle_withValidCommand_movesCourierAndUpdates() {
        var courier = Courier.create("Иван", L_5_5).getValueOrThrow();
        var command = MoveCourierCommand.create(courier.getId(), L_6_5).getValueOrThrow();
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(L_6_5);
        verify(courierRepository).update(courier);
    }

    @Test
    void handle_whenCourierNotFound_returnsFailure() {
        var courierId = UUID.randomUUID();
        var command = MoveCourierCommand.create(courierId, L_6_5).getValueOrThrow();
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void handle_whenLocationTooFar_returnsFailure() {
        var courier = Courier.create("Иван", L_5_5).getValueOrThrow();
        var command = MoveCourierCommand.create(courier.getId(), L_10_10).getValueOrThrow();
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_withNullCourierId_returnsFailure() {
        var result = MoveCourierCommand.create(null, L_5_5);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_withNullLocation_returnsFailure() {
        var result = MoveCourierCommand.create(UUID.randomUUID(), null);

        assertThat(result.isFailure()).isTrue();
    }
}
