package com.parsing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 内服务组件 model
 * @author wugang
 * @since 2021-12-8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SerModel {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("版本号")
    private String version;

}
