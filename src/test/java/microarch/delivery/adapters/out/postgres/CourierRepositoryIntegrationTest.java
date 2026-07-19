package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.out.CourierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static microarch.delivery.testfixtures.TestLocations.L_1_1;
import static microarch.delivery.testfixtures.TestLocations.L_3_4;
import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestLocations.L_6_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_3;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourierRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    CourierRepository repository;

    @Test
    void CanAddCourier() {
        var courier = Courier.create("Alice", L_5_5).getValueOrThrow();

        repository.save(courier);
        var loaded = repository.findById(courier.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getName()).isEqualTo(courier.getName());
        assertThat(loaded.get().getLocation()).isEqualTo(courier.getLocation());
    }

    @Test
    void CanUpdateCourier() {
        var courier = Courier.create("Bob", L_5_5).getValueOrThrow();
        repository.save(courier);

        courier.move(L_6_5).getOrElseThrow();
        repository.update(courier);

        var loaded = repository.findById(courier.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getLocation()).isEqualTo(L_6_5);
    }

    @Test
    void CanGetAllCouriers() {
        repository.save(Courier.create("Alice", L_1_1).getValueOrThrow());
        repository.save(Courier.create("Bob", L_5_5).getValueOrThrow());

        var result = repository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void CanAddCourierWithAssignment() {
        var courier = Courier.create("Charlie", L_5_5).getValueOrThrow();
        courier.addAssignment(UUID.randomUUID(), VOLUME_3, L_3_4).getOrElseThrow();

        repository.save(courier);
        var loaded = repository.findById(courier.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getAssignments()).hasSize(1);
        assertThat(loaded.get().getAssignments().getFirst().getVolume()).isEqualTo(VOLUME_3);
    }
}
