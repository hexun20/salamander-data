package alanna.salamander.data.tape.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * mybatis-plus基础配置类
 *
 * @author alanna
 * @since 1.0
 */
@EnableTransactionManagement
@Configuration
public class MybatisPlusConfig {

    /**
     * 数据库类型
     * 参考：{@link DbType}
     * 默认MySQL
     */
    @Value("${salamander.orm.db-type:mysql}")
    private String dbType;

    // 分页插件
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.getDbType(dbType)));
        return interceptor;
    }
}
