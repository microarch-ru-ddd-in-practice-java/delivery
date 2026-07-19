package microarch.delivery.core.ports.out;

import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Порт для доступа к хранилищу курьеров.
 *
 * <p>
 * Абстракция репозитория курьеров. Предоставляет выборки в доменной терминологии и скрывает инфраструктурные детали.
 */
public interface CourierRepository {

    /**
     * Сохраняет нового курьера.
     *
     * @param courier курьер для сохранения; не должен быть {@code null}
     */
    void save(Courier courier);

    /**
     * Обновляет существующего курьера.
     *
     * @param courier курьер для обновления; не должен быть {@code null}
     */
    void update(Courier courier);

    /**
     * Находит курьера по идентификатору.
     *
     * @param id идентификатор курьера; не должен быть {@code null}
     * @return {@code Optional} с курьером, либо пустой {@code Optional} если курьер не найден
     */
    Optional<Courier> findById(UUID id);

    /**
     * Возвращает всех курьеров.
     *
     * @return список всех курьеров; пустой список если курьеров нет
     */
    List<Courier> findAll();
}
