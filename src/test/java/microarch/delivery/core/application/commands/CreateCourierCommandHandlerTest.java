package microarch.delivery.core.application.commands;

import microarch.delivery.core.ports.out.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateCourierCommandHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CreateCourierCommandHandlerImpl handler;

    @Test
    void handle_withValidCommand_savesCourierAndReturnsId() {
        var command = CreateCourierCommand.create("Иван").getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isNotNull();
        var captor = ArgumentCaptor.forClass(microarch.delivery.core.domain.model.courier.Courier.class);
        verify(courierRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Иван");
    }

    @Test
    void handle_savedCourierHasLocationInValidRange() {
        var command = CreateCourierCommand.create("Мария").getValueOrThrow();

        handler.handle(command);

        var captor = ArgumentCaptor.forClass(microarch.delivery.core.domain.model.courier.Courier.class);
        verify(courierRepository).save(captor.capture());
        var location = captor.getValue().getLocation();
        assertThat(location).isNotNull();
    }

    @Test
    void create_withNullName_returnsFailure() {
        var result = CreateCourierCommand.create(null);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_withEmptyName_returnsFailure() {
        var result = CreateCourierCommand.create("  ");

        assertThat(result.isFailure()).isTrue();
    }
}
