package microarch.delivery.testfixtures;

import microarch.delivery.core.domain.model.kernel.Location;

public class TestLocations {

    public static final Location L_1_1 = Location.create(1, 1).getValue();
    public static final Location L_3_4 = Location.create(3, 4).getValue();
    public static final Location L_5_4 = Location.create(5, 4).getValue();
    public static final Location L_5_5 = Location.create(5, 5).getValue();
    public static final Location L_6_5 = Location.create(6, 5).getValue();
    public static final Location L_10_10 = Location.create(10, 10).getValue();

    private TestLocations() {
    }
}
