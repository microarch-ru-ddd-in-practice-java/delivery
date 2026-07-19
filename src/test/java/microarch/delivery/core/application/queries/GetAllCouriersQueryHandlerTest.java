package microarch.delivery.core.application.queries;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.out.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static microarch.delivery.testfixtures.TestLocations.L_1_1;
import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllCouriersQueryHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private GetAllCouriersQueryHandlerImpl handler;

    @Test
    void handle_returnsDtoForEachCourier() {
        var courier1 = Courier.create("Иван", L_1_1).getValueOrThrow();
        var courier2 = Courier.create("Мария", L_5_5).getValueOrThrow();
        when(courierRepository.findAll()).thenReturn(List.of(courier1, courier2));

        var result = handler.handle();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).hasSize(2);
        assertThat(result.getValue()).extracting("name").containsExactly("Иван", "Мария");
    }

    @Test
    void handle_whenNoCouriers_returnsEmptyList() {
        when(courierRepository.findAll()).thenReturn(List.of());

        var result = handler.handle();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEmpty();
    }

    @Test
    void handle_dtosContainCorrectLocationAndId() {
        var courier = Courier.create("Иван", L_1_1).getValueOrThrow();
        when(courierRepository.findAll()).thenReturn(List.of(courier));

        var result = handler.handle();

        var dto = result.getValue().getFirst();
        assertThat(dto.id()).isEqualTo(courier.getId());
        assertThat(dto.location()).isEqualTo(L_1_1);
    }
}
