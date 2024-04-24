package alanna.salamander.data.redis;

import alanna.salamander.data.redis.config.RedisConfig;
import alanna.salamander.data.redis.ops.RedisOperation;
import alanna.salamander.data.redis.ops.Type;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * test for redis operation
 *
 * @author alanna
 * @since 0.1
 */
public class RedisOperationTest {

    /**
     * redis test key
     */
    private static final String KEY = "test_key";

    /**
     * redis connection factory
     */
    private final LettuceConnectionFactory connectionFactory =
            new LettuceConnectionFactory("192.168.56.101", 6379);

    /**
     * the redis template to get value
     */
    private final RedisTemplate<String, String> redisTemplate =
            RedisConfig.newRedisTemplate(connectionFactory, String.class);

    /**
     * redis operation object to test
     */
    private final RedisOperation redisOperation = new RedisOperation(redisTemplate);

    {
        connectionFactory.afterPropertiesSet();
    }

    @Before
    public void before() {
        setTestValue();
    }

    @After
    public void after() {
        deleteTestValue();
    }

    @Test
    public void testGet() {
        Map<String, Object> value = getTestValue();
        Assert.assertEquals("张三", value.get("name"));
    }

    @Test
    public void testSet() {
        Boolean result = persist();
        Assert.assertNotNull(result);
        Assert.assertTrue(result);
    }

    @Test
    public void testDelete() {
        redisOperation.delete(KEY);
        Boolean result = persist();
        Assert.assertNotNull(result);
        Assert.assertFalse(result);
    }

    private Boolean persist() {
        return redisOperation.persist(KEY);
    }

    private void setTestValue() {
        Map<String, Object> valueObj = new HashMap<>(3);
        valueObj.put("name", "张三");
        valueObj.put("age", 30);
        valueObj.put("sex", "男");
        redisOperation.setValueFor(KEY)
                .timeout(5)
                .timeUnit(TimeUnit.SECONDS)
                .set(valueObj);
    }

    private Map<String, Object> getTestValue() {
        return redisOperation.getValueFor(new Type<Map<String, Object>>())
                .get(KEY);
    }

    private void deleteTestValue() {
        redisOperation.delete(KEY);
    }

    private LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("192.168.56.101", 6379);
    }
}
