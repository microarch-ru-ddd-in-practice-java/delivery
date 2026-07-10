package microarch.delivery.core.domain.model.courier;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentTest {

    @Test
    void createInitializesAssignmentWithAssignedStatus() {
        UUID orderId = UUID.randomUUID();
        Volume volume = Volume.create(3).getValue();
        Location location = new Location(5, 7);

        Result<Assignment, Error> result = Assignment.create(orderId, volume, location);

        assertThat(result.isSuccess()).isTrue();
        Assignment assignment = result.getValue();
        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getOrderId()).isEqualTo(orderId);
        assertThat(assignment.getVolume()).isEqualTo(volume);
        assertThat(assignment.getLocation()).isEqualTo(location);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }
}
