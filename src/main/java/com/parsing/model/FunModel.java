package com.parsing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * 功能项 model
 * @author wugang
 * @since 2021-12-8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class FunModel {

    @ApiModelProperty("功能项id")
    private String id;

    @ApiModelProperty("功能项名称")
    private String title;

    @ApiModelProperty("功能项描述")
    private String description;

    @ApiModelProperty("父项id")
    private String parent;

    @ApiModelProperty("关联功能项id")
    private String relate;

    @ApiModelProperty("兄弟项排序")
    private String next2;

    @ApiModelProperty("功能项所属类型")
    private Integer opTypes;

    @ApiModelProperty("依赖服务")
    private List<String> dependency;

    @ApiModelProperty("功能项路由")
    private List<PatternModel> patterns;

    @ApiModelProperty("扩展标签信息")
    private List<LabelModel> labels;

    @ApiModelProperty("默认绑定的角色信息")
    private List<String> roles;

    @ApiModelProperty("子级功能项")
    private List<FunModel> functions;

}
