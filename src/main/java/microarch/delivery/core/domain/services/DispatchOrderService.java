package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;

import java.util.List;

/**
 * Доменный сервис распределения заказов на курьеров.
 *
 * <p>
 * За один вызов диспетчеризует ровно один заказ: находит курьера с минимальным расстоянием до заказа среди тех, кто
 * способен его принять (не переполнен), и приводит курьера и заказ в согласованное состояние.
 */
public interface DispatchOrderService {

    /**
     * Распределяет заказ на наиболее подходящего курьера.
     *
     * <p>
     * Курьеры, у которых суммарный объём назначений плюс объём заказа превысит максимально допустимый, в отборе не
     * участвуют. Из оставшихся выбирается ближайший к местоположению заказа. При успехе курьер получает назначение,
     * заказ переходит в статус ASSIGNED.
     *
     * @param order
     *            заказ в статусе {@code CREATED}; не должен быть {@code null}
     * @param couriers
     *            список доступных курьеров; не должен быть {@code null}
     *
     * @return {@code Result.success} с назначенным курьером, либо {@code Result.failure} если нет доступных курьеров
     *
     * @throws NullPointerException
     *             если {@code order} или {@code couriers} равны {@code null}
     */
    Result<Courier, Error> dispatch(Order order, List<Courier> couriers);

    class Errors {
        public static Error noCourierAvailable() {
            return Error.of("dispatch.no.courier.available", "No courier available to take the order");
        }
    }
}
