package microarch.delivery.testfixtures;

import microarch.delivery.core.domain.model.kernel.Volume;

public class TestVolumes {

    public static final Volume VOLUME_3 = Volume.create(3).getValue();
    public static final Volume VOLUME_5 = Volume.create(5).getValue();
    public static final Volume VOLUME_20 = Volume.create(20).getValue();
    public static final Volume VOLUME_21 = Volume.create(21).getValue();

    private TestVolumes() {
    }
}
