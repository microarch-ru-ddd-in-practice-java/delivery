package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Команда создания заказа на доставку.
 *
 * <p>
 * Принимает идентификатор заказа (из корзины) и адрес доставки. Местоположение назначается случайным образом
 * обработчиком команды.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateOrderCommand {

    private final UUID orderId;
    private final int volume;

    /**
     * Создаёт команду с валидацией параметров.
     *
     * @param orderId идентификатор заказа; обязателен
     * @param volume  объём заказа; должен быть не отрицательным
     * @return {@code Result.success} с командой, либо {@code Result.failure} если параметр невалиден
     */
    public static Result<CreateOrderCommand, Error> create(UUID orderId, int volume) {
        var err = Guard.againstNullOrEmpty(orderId, "orderId");
        if (err != null)
            return Result.failure(err);

        return Result.success(new CreateOrderCommand(orderId, volume));
    }
}
