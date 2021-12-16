package com.parsing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组件依赖项 model
 * @author wugang
 * @since 2021-12-8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DependencyModel {

    @ApiModelProperty("内服务组件数组")
    private List<SerModel> services;

    @ApiModelProperty("第三方组件数组")
    private List<ComModel> compontents;

}
