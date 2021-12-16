package com.parsing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 视图内容 model
 * @author wugang
 * @since 2021-12-8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ConModel {

    @ApiModelProperty("功能项id")
    private String id;

    @ApiModelProperty("父项id")
    private String parent;

    @ApiModelProperty("兄弟项id")
    private List<ConModel> next2;

}
