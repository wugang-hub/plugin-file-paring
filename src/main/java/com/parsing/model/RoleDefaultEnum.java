package com.parsing.model;

/**
 * 枚举：默认角色及所属对象类型
 * @author wugang
 * @since 2021-12-9
 */
public enum RoleDefaultEnum {
    //baas
    SUPER(1,"超级管理员", 1),
    MANAGER(2,"系统管理员", 1),
    NORMAL(3,"一般用户", 1),
    //联盟
    FED_OWNER(4,"联盟所有者", 2),
    FED_MANAGER(5,"联盟管理者", 2),
    FED_MEMBER(6,"联盟成员", 2),
    //企业
    ORG_OWNER(7,"企业所有者", 3),
    ORG_MANAGER(8,"企业管理者", 3),
    ORG_MEMBER(9,"企业员工", 3);


    private Integer value;
    private String name;
    private Integer type; //默认角色对应的对象类型，非角色类型

    RoleDefaultEnum(Integer value, String name, Integer type) {
        this.value = value;
        this.name = name;
        this.type = type;
    }
    public Integer getValue() {
        return value;
    }
    public String getName() {
        return name;
    }
    public Integer getType() {
        return type;
    }

    public static Integer getValue(String name) {
        Integer value = 3;
        for(RoleDefaultEnum item: values()){
           if(item.name.equals(name)){
               value = item.value;
           }
        }
        return value;
    }

    public static Integer getType(String name) {
        Integer type = 3;
        for(RoleDefaultEnum item: values()){
            if(item.name.equals(name)){
                type = item.type;
            }
        }
        return type;
    }

}
