package microarch.delivery.adapters.in.http;

import api.ApiApi;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.application.commands.CompleteOrderCommand;
import microarch.delivery.core.application.commands.CompleteOrderCommandHandler;
import microarch.delivery.core.application.commands.CreateCourierCommand;
import microarch.delivery.core.application.commands.CreateCourierCommandHandler;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import microarch.delivery.core.application.commands.MoveCourierCommand;
import microarch.delivery.core.application.commands.MoveCourierCommandHandler;
import microarch.delivery.core.application.queries.GetAllCouriersQuery;
import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import microarch.delivery.core.application.queries.GetAllIncompleteOrdersQuery;
import microarch.delivery.core.application.queries.GetAllIncompleteOrdersQueryHandler;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.application.queries.dto.IncompleteOrderDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import model.Address;
import model.Courier;
import model.CreateCourierResponse;
import model.CreateOrderResponse;
import model.Location;
import model.NewCourier;
import model.NewOrder;
import model.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class DeliveryHttpController implements ApiApi {

    private final CompleteOrderCommandHandler completeOrder;
    private final CreateCourierCommandHandler createCourier;
    private final CreateOrderCommandHandler createOrder;
    private final GetAllCouriersQueryHandler getCouriers;
    private final GetAllIncompleteOrdersQueryHandler getOrders;
    private final MoveCourierCommandHandler moveCourier;

    public DeliveryHttpController(
            CompleteOrderCommandHandler completeOrder,
            CreateCourierCommandHandler createCourier,
            CreateOrderCommandHandler createOrder,
            GetAllCouriersQueryHandler getCouriers,
            GetAllIncompleteOrdersQueryHandler getOrders,
            MoveCourierCommandHandler moveCourier
    ) {
        this.completeOrder = completeOrder;
        this.createCourier = createCourier;
        this.createOrder = createOrder;
        this.getCouriers = getCouriers;
        this.getOrders = getOrders;
        this.moveCourier = moveCourier;
    }

    @Override
    public ResponseEntity<Void> completeOrder(UUID courierId, UUID orderId) {
        UnitResult<libs.errs.Error> result = completeOrder.handle(
                new CompleteOrderCommand(courierId, orderId)
        );

        if (result.isFailure()) {
            return error(result.getError());
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<CreateCourierResponse> createCourier(NewCourier newCourier) {
        if (newCourier == null) {
            return badRequest("newCourier");
        }

        Result<UUID, libs.errs.Error> result = createCourier.handle(
                new CreateCourierCommand(newCourier.getName())
        );

        if (result.isFailure()) {
            return error(result.getError());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateCourierResponse(result.getValue()));
    }

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(NewOrder newOrder) {
        if (newOrder == null) {
            return badRequest("newOrder");
        }

        Address address = newOrder.getAddress();
        if (address == null) {
            return badRequest("address");
        }

        Integer volume = newOrder.getVolume();
        if (volume == null) {
            return badRequest("volume");
        }

        UnitResult<libs.errs.Error> result = createOrder.handle(new CreateOrderCommand(
                newOrder.getId(),
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment(),
                volume
        ));

        if (result.isFailure()) {
            return error(result.getError());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateOrderResponse(newOrder.getId()));
    }

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {
        List<Courier> response = getCouriers.handle(new GetAllCouriersQuery()).stream()
                .map(DeliveryHttpController::toCourier)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        List<Order> response = getOrders.handle(new GetAllIncompleteOrdersQuery()).stream()
                .map(DeliveryHttpController::toOrder)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> moveCourier(UUID courierId, Location location) {
        if (location == null) {
            return badRequest("location");
        }

        Integer x = location.getX();
        Integer y = location.getY();

        if (x == null || y == null) {
            return badRequest("location");
        }

        UnitResult<libs.errs.Error> result = moveCourier.handle(
                new MoveCourierCommand(courierId, x, y)
        );

        if (result.isFailure()) {
            return error(result.getError());
        }

        return ResponseEntity.ok().build();
    }

    private static Courier toCourier(CourierDto courier) {
        return new Courier(courier.id(), courier.name(), toLocation(courier.location()));
    }

    private static Order toOrder(IncompleteOrderDto order) {
        return new Order(order.id(), toLocation(order.location()));
    }

    private static Location toLocation(LocationDto location) {
        return new Location(location.x(), location.y());
    }

    private static <T> ResponseEntity<T> badRequest(String fieldName) {
        return error(HttpStatus.BAD_REQUEST, GeneralErrors.valueIsRequired(fieldName).getMessage());
    }

    private static <T> ResponseEntity<T> error(libs.errs.Error error) {
        return error(httpStatus(error), error.getMessage());
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<T> error(HttpStatus status, String message) {
        return (ResponseEntity<T>) ResponseEntity.status(status)
                .body(new model.Error(status.value(), message));
    }

    private static HttpStatus httpStatus(libs.errs.Error error) {
        return switch (error.getCode()) {
            case "value.is.required",
                    "value.is.invalid",
                    "invalid.string.length",
                    "value.is.out.of.range",
                    "value.must.be.greater.than",
                    "value.must.be.greater.or.equal",
                    "value.must.be.less.than",
                    "value.must.be.less.or.equal" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.CONFLICT;
        };
    }
}
