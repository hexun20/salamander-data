package alanna.salamander.data.tape.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专为数据库主键存在默认值的表设计
 *
 * @author alanna
 * @since 0.1
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class BaseAutoIdEntity<T extends Serializable> {

    /**
     * 主键ID，表的主键字段必须具有默认值。
     */
    @TableId(type = IdType.AUTO)
    private T id;

    /**
     * 数据创建时间
     */
    @TableField
    private LocalDateTime createTime;

    /**
     * 数据修改时间
     */
    @TableField
    private LocalDateTime updateTime;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
