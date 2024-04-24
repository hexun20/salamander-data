package alanna.salamander.data.redis.ops;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作对象
 *
 * @author alanna
 * @since 0.1
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@ConditionalOnProperty(prefix = "salamander.data.redis", name = "power", havingValue = "on")
@Component
public class RedisOperation {

    /**
     * jackson json操作对象
     * 用于对结果数据进行序列化和反序列化
     */
    private static final ObjectMapper VALUE_MAPPER = new ObjectMapper();

    /**
     * 核心的Redis操作对象，SpringDataRedis的RedisTemplate。
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 默认key的过期时间，单位秒。
     */
    private final long defaultExpireTimeout = 24L * 60 * 60;

    @Autowired
    public RedisOperation(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建GetValue操作对象
     *
     * @param resultType 结果数据的封装类型
     * @return key's value object
     */
    public <T> GetValueOperation<T> getValueFor(Type<T> resultType) {
        return new GetValueOperation<>(resultType);
    }

    /**
     * 用于读取数据的对象
     *
     * @param <T> 结果数据类型，用于封装读取到的数据。
     */
    public class GetValueOperation<T> {

        private final Type<T> resultType;

        private long timeout = defaultExpireTimeout;

        private TimeUnit timeUnit = TimeUnit.SECONDS;

        /**
         * 生成GetValueOperation
         *
         * @param resultType 结果类型封装类型
         */
        public GetValueOperation(Type<T> resultType) {
            this.resultType = resultType;
        }

        /**
         * 设置过期时长
         *
         * @param timeout key的过期时长
         * @return GetValueOperation
         */
        public GetValueOperation<T> timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * 设置过期的时间单位
         *
         * @param timeUnit 时间单位
         * @return GetValueOperation
         */
        public GetValueOperation<T> timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        /**
         * 获取key值
         *
         * @param key redis key
         * @return key值
         */
        public T get(String key) {
            return parseValue(redisTemplate.opsForValue().get(key));
        }

        /**
         * 获取key值，并设置该key的过期时间。
         *
         * @param key redis key
         * @return key's value
         */
        public T getAndExpire(String key) {
            return parseValue(redisTemplate.opsForValue().getAndExpire(key, timeout, timeUnit));
        }

        /**
         * 获取key值并删除key
         *
         * @param key redis key
         * @return key's value
         */
        public T getAndDelete(String key) {
            return parseValue(redisTemplate.opsForValue().getAndDelete(key));
        }

        private T parseValue(String value) {
            return Optional.ofNullable(value)
                    .map(v -> convertTo(v, resultType))
                    .orElse(null);
        }
    }

    /**
     * 创建SetValue操作对象
     *
     * @param key redis key
     * @return SetValueOperation
     */
    public SetValueOperation setValueFor(String key) {
        return new SetValueOperation(key);
    }

    /**
     * the operation object for set value
     */
    public class SetValueOperation {

        private final String key;

        private long timeout = defaultExpireTimeout;

        private TimeUnit timeUnit = TimeUnit.SECONDS;

        /**
         * 生成SetValueOperation
         *
         * @param key redis key
         */
        public SetValueOperation(String key) {
            this.key = key;
        }

        /**
         * 设置过期时长
         *
         * @param timeout key的过期时长
         * @return SetValueOperation
         */
        public SetValueOperation timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * 设置过期的时间单位
         *
         * @param timeUnit 时间单位
         * @return SetValueOperation
         */
        public SetValueOperation timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        /**
         * 将数据写入Redis
         */
        public void set(Object value) {
            redisTemplate.opsForValue().set(key, writeValueAsString(value), timeout, timeUnit);
        }
    }

    /**
     * 删除key
     *
     * @param key redis key
     * @return Boolean
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     *
     * @param key redis key
     * @return Boolean
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    private static <T> T convertTo(String value, Type<T> t) {
        try {
            return VALUE_MAPPER.readValue(value, t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

    private static String writeValueAsString(Object value) {
        try {
            return VALUE_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
