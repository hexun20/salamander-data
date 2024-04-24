package alanna.salamander.data.tape.config;

import alanna.salamander.data.tape.util.GeometryUtils;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import mil.nga.sf.geojson.Geometry;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.postgis.PGgeometry;
import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGobject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Postgresql配置类
 *
 * @author alanna
 * @since 1.0
 */
@ConditionalOnProperty(prefix = "salamander.data", name = "db-type", havingValue = "postgresql")
@Configuration
public class PostgresqlMybatisPlusConfig {

    /**
     * 注册自定义处理器
     */
    @Bean
    ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            configuration.getTypeHandlerRegistry().register(JSONObject.class, JsonTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(Geometry.class, GeometryTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(List.class, JSONArrayTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(PGobject.class, PGObjectTypeHandler.class);
        };
    }

    public static class PGObjectTypeHandler implements TypeHandler<PGobject> {

        @Override
        public void setParameter(PreparedStatement ps, int i, PGobject parameter, JdbcType jdbcType) throws SQLException {
            ps.setObject(i, parameter);
        }

        @Override
        public PGobject getResult(ResultSet rs, String columnName) throws SQLException {
            return (PGobject) rs.getObject(columnName);
        }

        @Override
        public PGobject getResult(ResultSet rs, int columnIndex) throws SQLException {
            return (PGobject) rs.getObject(columnIndex);
        }

        @Override
        public PGobject getResult(CallableStatement cs, int columnIndex) throws SQLException {
            return (PGobject) cs.getObject(columnIndex);
        }
    }

    /**
     * Jsonb to JSONObject
     */
    public static class JsonTypeHandler implements TypeHandler<Map<String, ?>> {

        public JsonTypeHandler() {
        }

        @Override
        public void setParameter(PreparedStatement ps, int i, Map<String, ?> parameter, JdbcType jdbcType) throws SQLException {
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(null != parameter ? JSONObject.toJSONString(parameter) : null);
            ps.setObject(i, pGobject);
        }

        @Override
        public Map<String, ?> getResult(ResultSet rs, String columnName) throws SQLException {
            return JSONObject.parseObject(rs.getString(columnName));
        }

        @Override
        public Map<String, ?> getResult(ResultSet rs, int columnIndex) throws SQLException {
            return JSONObject.parseObject(rs.getString(columnIndex));
        }

        @Override
        public Map<String, ?> getResult(CallableStatement cs, int columnIndex) throws SQLException {
            return JSONObject.parseObject(cs.getString(columnIndex));
        }
    }

    /**
     * Postgis Geometry to Geojson
     */
    public static class GeometryTypeHandler implements TypeHandler<Geometry> {

        @Override
        public void setParameter(PreparedStatement ps, int i,
                                 Geometry parameter, JdbcType jdbcType) throws SQLException {
            ps.setObject(i, GeometryUtils.convert2PGeometry(parameter));
        }

        @Override
        public Geometry getResult(ResultSet rs, String columnName) throws SQLException {

            return handlerPGeometry(rs.getObject(columnName));
        }

        @Override
        public Geometry getResult(ResultSet rs, int columnIndex) throws SQLException {
            return handlerPGeometry(rs.getObject(columnIndex));

        }

        @Override
        public Geometry getResult(CallableStatement cs, int columnIndex) throws SQLException {
            return handlerPGeometry(cs.getObject(columnIndex));
        }

        private Geometry handlerPGeometry(Object object) throws SQLException {
            if (object instanceof PGgeometry) {
                PGgeometry geom = (PGgeometry) object;
                return GeometryUtils.convert2GGeometry(geom);
            }
            return null;
        }
    }

    /**
     * Jsonb to JSONObject
     */
    public static class JSONArrayTypeHandler implements TypeHandler<List<?>> {

        public JSONArrayTypeHandler() {
        }

        @Override
        public void setParameter(PreparedStatement ps, int i, List<?> list, JdbcType jdbcType) throws SQLException {
            if (CollectionUtils.isEmpty(list)) {
                ps.setArray(i, null);
                return;
            }
            Object[] array = list.toArray();
            Connection conn = ps.getConnection();
            String typeName = getTypeName(array);
            Array value = conn.createArrayOf(typeName, array);
            ps.setArray(i, value);
        }

        @Override
        public List<?> getResult(ResultSet rs, String columnName) throws SQLException {
            PgArray array = (PgArray) rs.getArray(columnName);
            if (Objects.isNull(array)) {
                return null;
            }
            Object[] a = (Object[]) array.getArray();
            return Arrays.asList(a);
        }

        @Override
        public List<?> getResult(ResultSet rs, int columnIndex) throws SQLException {
            PgArray array = (PgArray) rs.getArray(columnIndex);
            if (Objects.isNull(array)) {
                return null;
            }
            Object[] a = (Object[]) array.getArray();
            return Arrays.asList(a);
        }

        @Override
        public List<?> getResult(CallableStatement cs, int columnIndex) throws SQLException {
            PgArray array = (PgArray) cs.getArray(columnIndex);
            if (Objects.isNull(array)) {
                return null;
            }
            Object[] a = (Object[]) array.getArray();
            return Arrays.asList(a);
        }

        private String getTypeName(Object[] array) {
            Object e = array[0];
            if (e instanceof String) {
                return "varchar";
            }
            if (e instanceof Integer) {
                return "int4";
            }
            if (e instanceof Long) {
                return "int8";
            }
            if (e instanceof Double || e instanceof BigDecimal) {
                return "numeric";
            }
            if (e instanceof Boolean) {
                return "bool";
            }
            if (e instanceof Map) {
                return "jsonb";
            }
            return "varchar";
        }
    }
}
