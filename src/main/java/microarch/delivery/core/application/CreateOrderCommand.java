package microarch.delivery.core.application;

import java.util.UUID;

/**
 * Команда создания заказа.
 *
 * <p>
 * Формируется при получении события «Корзина оформлена» из Kafka. Id корзины становится Id заказа.
 *
 * @param basketId  Id корзины, он же Id создаваемого заказа
 * @param locationX горизонтальная координата доставки (1–10)
 * @param locationY вертикальная координата доставки (1–10)
 * @param volume    объём заказа
 */
public record CreateOrderCommand(UUID basketId, int locationX, int locationY, int volume) {
}
