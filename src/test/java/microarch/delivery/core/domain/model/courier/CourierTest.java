package microarch.delivery.core.domain.model.courier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CourierTest {

    private static final Location LOCATION = Location.create(5, 5).getValue();
    private static final Volume VOLUME_5 = Volume.create(5).getValue();

    private static Courier validCourier() {
        return Courier.create("Ivan", LOCATION).getValue();
    }

    // --- creation ---

    @Test
    void create_withValidParams_succeeds() {
        assertThat(Courier.create("Ivan", LOCATION).isSuccess()).isTrue();
    }

    @Test
    void create_withNullName_fails() {
        assertThat(Courier.create(null, LOCATION).isFailure()).isTrue();
    }

    @Test
    void create_withEmptyName_fails() {
        assertThat(Courier.create("", LOCATION).isFailure()).isTrue();
    }

    @Test
    void create_withNullLocation_fails() {
        assertThat(Courier.create("Ivan", null).isFailure()).isTrue();
    }

    // --- canTakeOrder ---

    @Test
    void canTakeOrder_whenEmpty_andVolumeWithinMax_returnsTrue() {
        assertThat(validCourier().canTakeOrder(Volume.create(20).getValue())).isTrue();
    }

    @Test
    void canTakeOrder_whenEmpty_andVolumeExceedsMax_returnsFalse() {
        assertThat(validCourier().canTakeOrder(Volume.create(21).getValue())).isFalse();
    }

    @Test
    void canTakeOrder_afterPartialLoad_returnsTrueIfFits() {
        var courier = validCourier();
        courier.addAssignment(UUID.randomUUID(), VOLUME_5, LOCATION);
        assertThat(courier.canTakeOrder(Volume.create(15).getValue())).isTrue();
    }

    @Test
    void canTakeOrder_afterPartialLoad_returnsFalseIfExceeds() {
        var courier = validCourier();
        courier.addAssignment(UUID.randomUUID(), VOLUME_5, LOCATION);
        assertThat(courier.canTakeOrder(Volume.create(16).getValue())).isFalse();
    }

    @Test
    void canTakeOrder_withNullVolume_throwsNullPointerException() {
        assertThatThrownBy(() -> validCourier().canTakeOrder(null)).isInstanceOf(NullPointerException.class);
    }

    // --- addAssignment ---

    @Test
    void addAssignment_whenHasCapacity_succeeds() {
        var courier = validCourier();
        assertThat(courier.addAssignment(UUID.randomUUID(), VOLUME_5, LOCATION).isSuccess()).isTrue();
    }

    @Test
    void addAssignment_whenHasCapacity_increasesAssignmentCount() {
        var courier = validCourier();
        courier.addAssignment(UUID.randomUUID(), VOLUME_5, LOCATION);
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void addAssignment_whenVolumeExceedsMax_fails() {
        var courier = validCourier();
        assertThat(courier.addAssignment(UUID.randomUUID(), Volume.create(21).getValue(), LOCATION).isFailure())
                .isTrue();
    }

    @Test
    void addAssignment_withNullOrderId_fails() {
        assertThat(validCourier().addAssignment(null, VOLUME_5, LOCATION).isFailure()).isTrue();
    }

    @Test
    void addAssignment_withNullVolume_fails() {
        assertThat(validCourier().addAssignment(UUID.randomUUID(), null, LOCATION).isFailure()).isTrue();
    }

    @Test
    void addAssignment_withNullLocation_fails() {
        assertThat(validCourier().addAssignment(UUID.randomUUID(), VOLUME_5, null).isFailure()).isTrue();
    }

    // --- completeAssignment ---

    @Test
    void completeAssignment_whenCourierAtSameLocation_succeeds() {
        var courier = validCourier();
        var orderId = UUID.randomUUID();
        courier.addAssignment(orderId, VOLUME_5, LOCATION);
        assertThat(courier.completeAssignment(orderId).isSuccess()).isTrue();
    }

    @Test
    void completeAssignment_whenCourierAdjacent_succeeds() {
        var courier = validCourier();
        var orderId = UUID.randomUUID();
        courier.addAssignment(orderId, VOLUME_5, Location.create(6, 5).getValue());
        assertThat(courier.completeAssignment(orderId).isSuccess()).isTrue();
    }

    @Test
    void completeAssignment_whenCourierTooFar_fails() {
        var courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();
        var orderId = UUID.randomUUID();
        courier.addAssignment(orderId, VOLUME_5, Location.create(5, 5).getValue());
        assertThat(courier.completeAssignment(orderId).isFailure()).isTrue();
    }

    @Test
    void completeAssignment_withUnknownOrderId_fails() {
        var courier = validCourier();
        assertThat(courier.completeAssignment(UUID.randomUUID()).isFailure()).isTrue();
    }

    @Test
    void completeAssignment_withNullOrderId_throwsNullPointerException() {
        assertThatThrownBy(() -> validCourier().completeAssignment(null)).isInstanceOf(NullPointerException.class);
    }

    // --- move ---

    @ParameterizedTest(name = "move to ({0},{1}) — distance <= 1")
    @CsvSource({ "5, 5", "6, 5", "4, 5", "5, 6", "5, 4" })
    void move_withinOneStep_succeeds(int x, int y) {
        var courier = validCourier();
        assertThat(courier.move(Location.create(x, y).getValue()).isSuccess()).isTrue();
    }

    @ParameterizedTest(name = "move to ({0},{1}) — distance > 1")
    @CsvSource({ "7, 5", "5, 7", "3, 3" })
    void move_moreThanOneStep_fails(int x, int y) {
        var courier = validCourier();
        assertThat(courier.move(Location.create(x, y).getValue()).isFailure()).isTrue();
    }

    @Test
    void move_updatesLocation() {
        var courier = validCourier();
        var newLocation = Location.create(6, 5).getValue();
        courier.move(newLocation);
        assertThat(courier.getLocation()).isEqualTo(newLocation);
    }

    @Test
    void move_withNullLocation_throwsNullPointerException() {
        assertThatThrownBy(() -> validCourier().move(null)).isInstanceOf(NullPointerException.class);
    }
}
