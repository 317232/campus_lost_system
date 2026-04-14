package com.campus.lostfound.domain.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("categories")
public class Category extends BaseEntity {


    @TableField("biz_id")
    private String bizId; // 分类业务编号

    private String name; // 分类名称

    @TableField("sort_order")
    private Integer sortOrder; // 排序值

    private String status; // 状态：ENABLED/DISABLED
}
