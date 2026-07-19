package microarch.delivery.core.ports.out;

import microarch.delivery.core.domain.model.order.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Порт для доступа к хранилищу заказов.
 *
 * <p>
 * Абстракция репозитория заказов. Предоставляет выборки в доменной терминологии и скрывает инфраструктурные детали.
 */
public interface OrderRepository {

    /**
     * Сохраняет новый заказ.
     *
     * @param order заказ для сохранения; не должен быть {@code null}
     */
    void save(Order order);

    /**
     * Обновляет существующий заказ.
     *
     * @param order заказ для обновления; не должен быть {@code null}
     */
    void update(Order order);

    /**
     * Находит заказ по идентификатору.
     *
     * @param id идентификатор заказа; не должен быть {@code null}
     * @return {@code Optional} с заказом, либо пустой {@code Optional} если заказ не найден
     */
    Optional<Order> findById(UUID id);

    /**
     * Находит первый заказ в статусе {@code CREATED}.
     *
     * @return {@code Optional} с заказом, либо пустой {@code Optional} если нет ни одного созданного заказа
     */
    Optional<Order> findFirstCreated();

    /**
     * Возвращает все заказы в статусе {@code ASSIGNED}.
     *
     * @return список назначенных заказов; пустой список если таковых нет
     */
    List<Order> findAllAssigned();
}
