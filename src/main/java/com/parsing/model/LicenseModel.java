package com.parsing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * license model
 * @author wugang
 * @since 2021-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class LicenseModel {

    @ApiModelProperty("编号")
    private String id;

    @ApiModelProperty("内容")
    private String title;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("是否为基础功能")
    private Boolean basic;

    @ApiModelProperty("依赖项")
    private String dependency;

    @ApiModelProperty("取值")
    private String value;

    @ApiModelProperty("父级id")
    private String parent;

    @ApiModelProperty("关联功能项")
    private List<String> functions;

    @ApiModelProperty("子级license")
    private List<LicenseModel> licenses;

}
