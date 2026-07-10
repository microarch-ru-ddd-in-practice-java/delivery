package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import microarch.delivery.ApplicationProperties;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.GeoClient;
import org.springframework.stereotype.Component;

@Component
public class GeoClientImpl implements GeoClient {

    private final ManagedChannel channel;
    private final GeoGrpc.GeoBlockingStub stub;

    public GeoClientImpl(ApplicationProperties properties) {
        this.channel = ManagedChannelBuilder
                .forAddress(properties.getGrpc().getGeoService().getHost(), properties.getGrpc().getGeoService().getPort())
                .usePlaintext()
                .build();
        this.stub = GeoGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        channel.shutdown();
    }

    @Override
    public Result<Location, Error> getGeolocation(String street) {
        Error validationError = Guard.againstNullOrEmpty(street, "street");

        if (validationError != null) {
            return Result.failure(validationError);
        }

        GeoProto.GetGeolocationRequest request = GeoProto.GetGeolocationRequest.newBuilder()
                .setStreet(street)
                .build();

        GeoProto.GetGeolocationReply reply = stub.getGeolocation(request);

        if (!reply.hasLocation()) {
            return Result.failure(Error.of(
                    "geo.location.is.required",
                    "Geo service returned response without location"));
        }

        GeoProto.Location geoLocation = reply.getLocation();
        Error locationValidationError = Guard.combine(
                Guard.againstOutOfRange(geoLocation.getX(), Location.MIN_COORDINATE,
                        Location.MAX_COORDINATE, "locationX"),
                Guard.againstOutOfRange(geoLocation.getY(), Location.MIN_COORDINATE,
                        Location.MAX_COORDINATE, "locationY"));

        if (locationValidationError != null) {
            return Result.failure(locationValidationError);
        }

        return Result.success(new Location(geoLocation.getX(), geoLocation.getY()));
    }
}
