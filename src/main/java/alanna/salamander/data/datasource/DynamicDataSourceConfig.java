package alanna.salamander.data.datasource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 动态数据源配置
 *
 * @author alanna
 * @since 0.1
 */
@ConditionalOnProperty(prefix = "salamander.data.dynamic-datasource", name = "power", havingValue = "on")
@Configuration
public class DynamicDataSourceConfig {

    @Bean
    public DynamicDataSource dynamicDataSource(DataSource dataSource) {
        return new DynamicDataSource(dataSource);
    }
}
