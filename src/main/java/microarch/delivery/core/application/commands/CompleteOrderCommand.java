package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Команда завершения доставки заказа курьером.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompleteOrderCommand {

    private final UUID courierId;
    private final UUID orderId;

    /**
     * Создаёт команду с валидацией параметров.
     *
     * @param courierId идентификатор курьера; обязателен
     * @param orderId   идентификатор заказа; обязателен
     * @return {@code Result.success} с командой, либо {@code Result.failure} если параметр невалиден
     */
    public static Result<CompleteOrderCommand, Error> create(UUID courierId, UUID orderId) {
        Error err = Guard.combine(
                Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(orderId, "orderId"));
        if (err != null)
            return Result.failure(err);

        return Result.success(new CompleteOrderCommand(courierId, orderId));
    }
}
