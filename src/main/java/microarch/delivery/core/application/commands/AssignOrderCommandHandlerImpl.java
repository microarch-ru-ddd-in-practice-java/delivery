package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.services.DispatchOrderService;
import microarch.delivery.core.ports.out.CourierRepository;
import microarch.delivery.core.ports.out.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Обработчик команды назначения заказа.
 *
 * <p>
 * Берёт первый заказ в статусе {@code CREATED}, находит ближайшего доступного курьера через
 * {@link DispatchOrderService} и сохраняет изменения обоих агрегатов в одной транзакции. Если нет заказов или нет
 * доступных курьеров — возвращает успех без действий (идемпотентность).
 */
@Service
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final DispatchOrderService dispatchOrderService;

    public AssignOrderCommandHandlerImpl(OrderRepository orderRepository, CourierRepository courierRepository,
            DispatchOrderService dispatchOrderService) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.dispatchOrderService = dispatchOrderService;
    }

    @Transactional
    @Override
    public UnitResult<Error> handle(AssignOrderCommand command) {
        var orderOpt = orderRepository.findFirstCreated();
        if (orderOpt.isEmpty())
            return UnitResult.success();

        var order = orderOpt.get();
        var couriers = courierRepository.findAll();

        var dispatchResult = dispatchOrderService.dispatch(order, couriers);
        if (dispatchResult.isFailure())
            return UnitResult.failure(dispatchResult.getError());

        var courier = dispatchResult.getValue();
        orderRepository.update(order);
        courierRepository.update(courier);
        return UnitResult.success();
    }
}
