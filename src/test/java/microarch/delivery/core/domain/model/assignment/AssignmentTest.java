package microarch.delivery.core.domain.model.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import microarch.delivery.core.domain.model.kernel.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static microarch.delivery.testfixtures.TestLocations.L_5_5;
import static microarch.delivery.testfixtures.TestVolumes.VOLUME_3;

class AssignmentTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();

    private static Assignment validAssignment() {
        return Assignment.create(ID, ORDER_ID, VOLUME_3, L_5_5).getValue();
    }

    // --- creation: valid ---

    @Test
    void create_withValidParams_succeeds() {
        assertThat(Assignment.create(ID, ORDER_ID, VOLUME_3, L_5_5).isSuccess()).isTrue();
    }

    @Test
    void create_withValidParams_statusIsAssigned() {
        var assignment = validAssignment();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    // --- creation: invalid ---

    @Test
    void create_withNullId_fails() {
        assertThat(Assignment.create(null, ORDER_ID, VOLUME_3, L_5_5).isFailure()).isTrue();
    }

    @Test
    void create_withNullOrderId_fails() {
        assertThat(Assignment.create(ID, null, VOLUME_3, L_5_5).isFailure()).isTrue();
    }

    @Test
    void create_withNullVolume_fails() {
        assertThat(Assignment.create(ID, ORDER_ID, null, L_5_5).isFailure()).isTrue();
    }

    @Test
    void create_withNullLocation_fails() {
        assertThat(Assignment.create(ID, ORDER_ID, VOLUME_3, null).isFailure()).isTrue();
    }

    // --- complete: success ---

    @ParameterizedTest(name = "courier at ({0},{1}) — distance <= 1")
    @CsvSource({ "5, 5", // same cell, distance 0
            "6, 5", // adjacent horizontal, distance 1
            "4, 5", // adjacent horizontal, distance 1
            "5, 6", // adjacent vertical, distance 1
            "5, 4" // adjacent vertical, distance 1
    })
    void complete_whenCourierWithinOneStep_succeeds(int x, int y) {
        var assignment = validAssignment();
        var courierLocation = Location.create(x, y).getValue();
        assertThat(assignment.complete(courierLocation).isSuccess()).isTrue();
    }

    @Test
    void complete_changesStatusToCompleted() {
        var assignment = validAssignment();
        assignment.complete(L_5_5);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    // --- complete: failure ---

    @ParameterizedTest(name = "courier at ({0},{1}) — distance > 1")
    @CsvSource({ "7, 5", // distance 2
            "5, 7", // distance 2
            "3, 3", // distance 4
            "1, 1" // distance 8
    })
    void complete_whenCourierTooFar_fails(int x, int y) {
        var assignment = validAssignment();
        var courierLocation = Location.create(x, y).getValue();
        assertThat(assignment.complete(courierLocation).isFailure()).isTrue();
    }

    @Test
    void complete_whenAlreadyCompleted_fails() {
        var assignment = validAssignment();
        assignment.complete(L_5_5);
        assertThat(assignment.complete(L_5_5).isFailure()).isTrue();
    }

    @Test
    void complete_withNullCourierLocation_throwsNullPointerException() {
        var assignment = validAssignment();
        assertThatThrownBy(() -> assignment.complete(null)).isInstanceOf(NullPointerException.class);
    }

    // --- equality ---

    @Test
    void equals_sameId_isTrue() {
        var a = Assignment.create(ID, ORDER_ID, VOLUME_3, L_5_5).getValue();
        var b = Assignment.create(ID, UUID.randomUUID(), VOLUME_3, L_5_5).getValue();
        assertThat(a).isEqualTo(b);
    }

    @Test
    void equals_differentId_isFalse() {
        var a = Assignment.create(UUID.randomUUID(), ORDER_ID, VOLUME_3, L_5_5).getValue();
        var b = Assignment.create(UUID.randomUUID(), ORDER_ID, VOLUME_3, L_5_5).getValue();
        assertThat(a).isNotEqualTo(b);
    }
}
