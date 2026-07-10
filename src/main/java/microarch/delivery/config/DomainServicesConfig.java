package microarch.delivery.config;

import microarch.delivery.core.domain.services.OrderDistributionDomainService;
import microarch.delivery.core.domain.services.OrderDistributionDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServicesConfig {

    @Bean
    public OrderDistributionDomainService orderDistributionDomainService() {
        return new OrderDistributionDomainServiceImpl();
    }
}
