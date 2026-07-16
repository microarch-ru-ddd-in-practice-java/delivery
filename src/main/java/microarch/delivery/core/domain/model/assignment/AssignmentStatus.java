package microarch.delivery.core.domain.model.assignment;

/**
 * Статус назначения заказа на курьера.
 */
public enum AssignmentStatus {

    /**
     * Заказ назначен курьеру, доставка в процессе.
     */
    ASSIGNED,

    /**
     * Доставка завершена, курьер достиг местоположения заказа.
     */
    COMPLETED
}
