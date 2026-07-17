package microarch.delivery.core.domain.model.order;

/**
 * Статус заказа.
 */
public enum OrderStatus {

    /** Заказ создан, ожидает назначения курьера. */
    CREATED,

    /** Заказ назначен курьеру, доставка в процессе. */
    ASSIGNED,

    /** Заказ доставлен и завершён. */
    COMPLETED
}
