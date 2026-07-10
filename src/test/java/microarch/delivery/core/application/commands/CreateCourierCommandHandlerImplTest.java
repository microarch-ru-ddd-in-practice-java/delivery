package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CreateCourierCommandHandlerImplTest {

    @Test
    void createsCourierAndSavesIt() {
        CourierRepository courierRepository = mock(CourierRepository.class);
        CreateCourierCommandHandler handler = new CreateCourierCommandHandlerImpl(courierRepository);

        Result<UUID, Error> result = handler.handle(new CreateCourierCommand("Ivan"));

        ArgumentCaptor<Courier> courierCaptor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).add(courierCaptor.capture());
        Courier courier = courierCaptor.getValue();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(courier.getId());
        assertThat(courier.getName()).isEqualTo("Ivan");
        assertThat(courier.getLocation()).isEqualTo(new Location(1, 1));
    }
}
