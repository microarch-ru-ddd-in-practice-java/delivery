package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MoveCourierCommandHandlerImplTest {

    @Test
    void changesCourierLocationAndSavesIt() {
        CourierRepository courierRepository = mock(CourierRepository.class);
        Courier courier = Courier.create("Ivan", new Location(1, 1)).getValue();
        when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
        MoveCourierCommandHandler handler = new MoveCourierCommandHandlerImpl(courierRepository);
        Location newLocation = new Location(4, 5);

        UnitResult<Error> result = handler.handle(new MoveCourierCommand(
                courier.getId(),
                newLocation.getX(),
                newLocation.getY()
        ));

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(newLocation);
        verify(courierRepository).update(courier);
    }
}
