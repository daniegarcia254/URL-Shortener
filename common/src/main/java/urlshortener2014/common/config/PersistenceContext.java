package urlshortener2014.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "urlshortener2014.common.repository" })
public class PersistenceContext {

}
