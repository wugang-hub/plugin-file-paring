package com.parsing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * yaml配置model
 * @author wugang
 * @since 2021-12-8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceModel {

    @ApiModelProperty("服务名称")
    private String module;

    @ApiModelProperty("服务描述信息")
    private String description;

    @ApiModelProperty("功能项定义")
    private List<FunModel> functions;

    @ApiModelProperty("license定义")
    private List<LicenseModel> licenses;

    @ApiModelProperty("视图定义")
    private List<ViewModel> views;

    @ApiModelProperty("组件定义")
    private DependencyModel dependency;

}

