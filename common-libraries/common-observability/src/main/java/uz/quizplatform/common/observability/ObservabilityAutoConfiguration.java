package uz.quizplatform.common.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ObservabilityAutoConfiguration {

    @Bean
    public FilterRegistrationBean<MdcCorrelationFilter> correlationFilterRegistration(MdcCorrelationFilter filter) {
        FilterRegistrationBean<MdcCorrelationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("mdcCorrelationFilter");
        registration.setOrder(1);
        return registration;
    }
}
