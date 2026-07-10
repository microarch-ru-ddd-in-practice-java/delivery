package microarch.delivery.core.application.queries;

import microarch.delivery.core.application.queries.dto.IncompleteOrderDto;

import java.util.List;

public interface GetAllIncompleteOrdersQueryHandler {

    List<IncompleteOrderDto> handle(GetAllIncompleteOrdersQuery query);
}
