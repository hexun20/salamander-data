package alanna.salamander.data.datasource;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author alanna
 * @since 0.1
 */
@ConditionalOnProperty(prefix = "salamander.data.dynamic-datasource", name = "power", havingValue = "on")
@Component
public class DataSourceRefresher {

    private final DynamicDataSource dynamicDataSource;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Value("${salamander.data.dynamic-datasource.shutdown-max-retry-times:10}")
    private int dataSourceShutdownMaxRetryTimes;

    @Autowired
    public DataSourceRefresher(DynamicDataSource dynamicDataSource) {
        this.dynamicDataSource = dynamicDataSource;
    }

    public synchronized void refreshDataSource(DataSource dataSource) {
        DataSource old = dynamicDataSource.getAndSetDataSource(dataSource);
        shutdown(old);
    }

    public void shutdown(DataSource dataSource) {
        scheduledExecutorService.execute(() -> shutdownDataSource(dataSource));
    }

    private void shutdownDataSource(DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            shutdownHikariDataSource((HikariDataSource) dataSource);
        }
    }

    private void shutdownHikariDataSource(HikariDataSource hikariDataSource) {
        int retryTimes = 0;
        HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
        while (poolBean.getActiveConnections() > 0 && retryTimes < dataSourceShutdownMaxRetryTimes) {
            poolBean.softEvictConnections();
        }
        hikariDataSource.close();
    }
}
