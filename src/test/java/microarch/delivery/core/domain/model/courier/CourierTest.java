package microarch.delivery.core.domain.model.courier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static microarch.delivery.testfixtures.TestLocations.L_1_1;
import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestLocations.L_6_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_20;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_21;

class CourierTest {

    private static Courier validCourier() {
        return Courier.create("Ivan", L_5_5).getValue();
    }

    // --- creation ---

    @Test
    void create_withValidParams_succeeds() {
        assertThat(Courier.create("Ivan", L_5_5).isSuccess()).isTrue();
    }

    @Test
    void create_withNullName_fails() {
        assertThat(Courier.create(null, L_5_5).isFailure()).isTrue();
    }

    @Test
    void create_withEmptyName_fails() {
        assertThat(Courier.create("", L_5_5).isFailure()).isTrue();
    }

    @Test
    void create_withNullLocation_fails() {
        assertThat(Courier.create("Ivan", null).isFailure()).isTrue();
    }

    // --- canTakeOrder ---

    @Test
    void canTakeOrder_whenEmpty_andVolumeWithinMax_returnsTrue() {
        assertThat(validCourier().canTakeOrder(VOLUME_20)).isTrue();
    }

    @Test
    void canTakeOrder_whenEmpty_andVolumeExceedsMax_returnsFalse() {
        assertThat(validCourier().canTakeOrder(VOLUME_21)).isFalse();
    }

    @Test
    void canTakeOrder_afterPartialLoad_returnsTrueIfFits() {
        var courier = validCourier();
        courier.addAssignment(UUID.randomUUID(), VOLUME_5, L_5_5);
        assertThat(courier.canTakeOrder(Volume.create(15).getValue())).isTrue();
    }

    @Test
    void canTakeOrder_afterPartialLoad_returnsFalseIfExceeds() {
        var courier = validCourier();
        courier.addAssignment(UUID.randomUUID(), VOLUME_5, L_5_5);
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
        assertThat(courier.addAssignment(UUID.randomUUID(), VOLUME_5, L_5_5).isSuccess()).isTrue();
    }

    @Test
    void addAssignment_whenHasCapacity_increasesAssignmentCount() {
        var courier = validCourier();
        courier.addAssignment(UUID.randomUUID(), VOLUME_5, L_5_5);
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void addAssignment_whenVolumeExceedsMax_fails() {
        var courier = validCourier();
        assertThat(courier.addAssignment(UUID.randomUUID(), VOLUME_21, L_5_5).isFailure()).isTrue();
    }

    @Test
    void addAssignment_withNullOrderId_fails() {
        assertThat(validCourier().addAssignment(null, VOLUME_5, L_5_5).isFailure()).isTrue();
    }

    @Test
    void addAssignment_withNullVolume_fails() {
        assertThat(validCourier().addAssignment(UUID.randomUUID(), null, L_5_5).isFailure()).isTrue();
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
        courier.addAssignment(orderId, VOLUME_5, L_5_5);
        assertThat(courier.completeAssignment(orderId).isSuccess()).isTrue();
    }

    @Test
    void completeAssignment_whenCourierAdjacent_succeeds() {
        var courier = validCourier();
        var orderId = UUID.randomUUID();
        courier.addAssignment(orderId, VOLUME_5, L_6_5);
        assertThat(courier.completeAssignment(orderId).isSuccess()).isTrue();
    }

    @Test
    void completeAssignment_whenCourierTooFar_fails() {
        var courier = Courier.create("Ivan", L_1_1).getValue();
        var orderId = UUID.randomUUID();
        courier.addAssignment(orderId, VOLUME_5, L_5_5);
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
        var newLocation = L_6_5;
        courier.move(newLocation);
        assertThat(courier.getLocation()).isEqualTo(newLocation);
    }

    @Test
    void move_withNullLocation_throwsNullPointerException() {
        assertThatThrownBy(() -> validCourier().move(null)).isInstanceOf(NullPointerException.class);
    }
}
