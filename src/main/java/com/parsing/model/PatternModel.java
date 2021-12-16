package com.parsing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 路由 model
 * @author wugang
 * @since 2021-12-8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PatternModel {

    @ApiModelProperty("url")
    private String path;

    @ApiModelProperty("请求方法")
    private String method;

}
