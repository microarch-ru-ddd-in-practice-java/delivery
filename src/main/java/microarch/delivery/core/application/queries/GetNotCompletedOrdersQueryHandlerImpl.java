package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Обработчик запроса на получение незавершённых заказов.
 */
@Service
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    @Override
    public Result<List<OrderDto>, Error> handle() {
        var orders = em.createQuery("SELECT o FROM Order o WHERE o.status <> :status", Order.class)
                .setParameter("status", OrderStatus.COMPLETED)
                .getResultList();
        var dtos = orders.stream()
                .map(o -> new OrderDto(o.getId(), o.getLocation()))
                .toList();
        return Result.success(dtos);
    }
}
