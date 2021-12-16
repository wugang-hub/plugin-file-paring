package com.parsing.utils;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSONObject;
import com.parsing.model.*;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据解析工具类
 * @author wugang
 * @since 2021-12-13
 */
public class AnalysisUtil {

    //权限结合sql数组
    private static List<String> opSqlList = new ArrayList<>();

    //默认角色和权限sql数组
    private static List<String> roleOpSqlList = new ArrayList<>();

    //URL和权限sql数组
    private static List<String> patternOpSqlList = new ArrayList<>();

    //License控制项数组
    private static List<JSONObject> licenseControlList = new ArrayList<>();
    //菜单控制项映射
    private static List<JSONObject> menuControlList = new ArrayList<>();
    //操作控制项映射
    private static List<JSONObject> operateControlList = new ArrayList<>();

    /**
     * sql:权限和路由
     * @param authId
     * @param url
     * @return
     */
    public static String toUrlPatternSQL(String authId, String url){
        return String.format("INSERT INTO auth_op_pattern VALUES('%s','%s');", authId, url);
    }

    /**
     * sql:权限和角色
     * @param authId
     * @param roleId
     * @param objType
     * @return
     */
    public static String toRolePatternSQL(String authId, Integer roleId, Integer objType){
        return String.format("INSERT INTO auth_role_op_default VALUES('%s','%s','%s');", roleId, objType, authId);
    }

    /**
     * sql:权限集合
     * @param authId
     * @param parentId
     * @param desc
     * @param name
     * @param relate
     * @param opType
     * @return
     */
    public static String toOpPatternSQL(String authId, String parentId, String desc, String name, String relate, Integer opType){
        return String.format("INSERT INTO auth_op(opId, parentId, relateId, name, description, opType) VALUES('%s','%s','%s','%s','%s','%s');", authId, parentId, relate, name, desc, opType);
    }

    private static void comment(String data, PrintWriter out){
        out.println("-- --------------------------------------------------------");
        out.println("-- "+data);
        out.println("-- --------------------------------------------------------");
    }

    /**
     * functions解析
     * @param funModelList
     * @return
     */
    public static void functionsAnalysis(List<FunModel> funModelList) {
        if(PlaceholderUtil.judgeCollect(funModelList)){
            funModelList.stream().forEach(funModel -> {
                //组装本身URL和权限sql
                patternToSql(funModel);
                //组装本身默认角色和权限sql
                roleOpDefaultToSql(funModel);
                //组装本身权限集合sql
                authOpToSql(funModel);
                //解析本身依赖服务
                List<String> depFunModels = funModel.getDependency();
                if(PlaceholderUtil.judgeCollect(depFunModels)){
                    depFunModels.stream().forEach(depFunModel->{
                        dependencyAnalysis(depFunModel);
                    });
                }
                //解析本身子级功能项
                List<FunModel> funModelChild = funModel.getFunctions();
                if(PlaceholderUtil.judgeCollect(funModelChild)){
                    functionsAnalysis(funModelChild);
                }
            });
        }
    }

    /**
     * 解析依赖服务的内容
     * @param depFunModel
     */
    private static void dependencyAnalysis(String depFunModel) {
        String serviceName = depFunModel.split("-")[0];
        String dependencyFunId = depFunModel.split("-")[1];
        if(!serviceName.equals("function") && !serviceName.equals("function")){
//            String filePath = serviceName + Const.FILE_PATH;
            String filePath = Const.FILE_PATH;
            try {
                //获取依赖服务信息
                String content = GitLabAPIUtil.getFileContentFromRepository(Const.REPO_IP, Const.PROJECT_PATH, Const.USERNAME, Const.PASSWORD, filePath, Const.BRANCH_NAME);
                YamlToolUtil toolUtil = new YamlToolUtil();
                toolUtil.initWithString(content);
                List<FunModel> funModelDependencyList = toolUtil.getServiceModel().getFunctions();
                List<FunModel> funModelsResult = dependencyFunctions(new ArrayList<>(), funModelDependencyList, dependencyFunId);
                //结合本身的信息生成需要的数据
                if(PlaceholderUtil.judgeCollect(funModelsResult)){
                    funModelsResult.stream().forEach(result -> {
                        //组装依赖-权限集合 auth_op
                        authOpToSql(result);
                        //组装依赖-URL auth_op_pattern
                        patternToSql(result);
                        //组装依赖-角色和权限关系 auth_role_op_default
                        roleOpDefaultToSql(result);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * dependencyFunctions解析
     * @param funModelsResult
     * @param funModelDependencyList
     * @param funId
     * @return
     */
    public static List<FunModel> dependencyFunctions(List<FunModel> funModelsResult, List<FunModel> funModelDependencyList, String funId) {
        List<FunModel> funModelsBack = funModelsResult;
        if(PlaceholderUtil.judgeCollect(funModelDependencyList)){
            funModelDependencyList.stream().forEach(funModel -> {
                String dependencyFunId = funModel.getId();
                if(dependencyFunId.equalsIgnoreCase(funId)){
                    funModelsBack.add(funModel);
                    //依赖的依赖
                    List<String> dependencyChildList = funModel.getDependency();
                    if(PlaceholderUtil.judgeCollect(dependencyChildList)){
                        dependencyChildList.stream().forEach(depFunModel->{
                            dependencyAnalysis(depFunModel);
                        });
                    }
                }else{
                    //获取子级
                    List<FunModel> funModelChild = funModel.getFunctions();
                    if(PlaceholderUtil.judgeCollect(funModelChild)){
                        dependencyFunctions(funModelsBack, funModelChild, funId);
                    }
                }
            });
        }
        return funModelsBack;
    }

    //权限集合sql
    private static void authOpToSql(FunModel funModel) {
        String parentId = funModel.getParent();
        String desc = funModel.getDescription();
        String name = funModel.getTitle();
        String relate = funModel.getRelate();
        Integer opType = funModel.getOpTypes();
        String next2 = funModel.getNext2();
        String sql = toOpPatternSQL(funModel.getId(), StringUtils.isEmpty(parentId) ? null : parentId, StringUtils.isEmpty(desc) ? null : desc, name, StringUtils.isEmpty(relate) ? null : relate, opType == null ? 1 : opType);
        if(!opSqlList.contains(sql)){
            opSqlList.add(sql);
        }
    }

    //URL和权限sql
    private static void patternToSql(FunModel funModel) {
        List<PatternModel> patternModels = funModel.getPatterns();
        if(PlaceholderUtil.judgeCollect(patternModels)){
            patternModels.stream().forEach(patternModel -> {
                //生成sql语句
                String url = patternModel.getMethod().toUpperCase() +" "+ patternModel.getPath();
                String sql = toUrlPatternSQL(funModel.getId(), url);
                if(!patternOpSqlList.contains(sql)){
                    patternOpSqlList.add(sql);
                }
            });
        }
    }

    //角色和权限sql
    private static void roleOpDefaultToSql(FunModel funModel) {
        List<String> roles = funModel.getRoles();
        if(PlaceholderUtil.judgeCollect(roles)){
            roles.stream().forEach(roleName -> {
                //生成sql语句
                Integer roleId = RoleDefaultEnum.getValue(roleName);
                Integer objType = RoleDefaultEnum.getType(roleName);
                String sql = toRolePatternSQL(funModel.getId(), roleId, objType);
                if(!roleOpSqlList.contains(sql)){
                    roleOpSqlList.add(sql);
                }
            });
        }
    }

    /**
     * functions解析
     * @param licenseModelList
     * @return
     */
    public static void licenseAnalysis(List<LicenseModel> licenseModelList, List<FunModel> funModelList) {
        if(PlaceholderUtil.judgeCollect(licenseModelList)){
            licenseModelList.stream().forEach(licenseModel -> {
                String licenseBh = licenseModel.getId();
                // license控制项
                assembleLicenseData(licenseModel);
                // 控制项映射 todo
                List<String> funIdList = licenseModel.getFunctions();
                if(PlaceholderUtil.judgeCollect(funIdList)){
                    funIdList.stream().forEach(funId -> {
                        // 菜单控制项映射
                        assembleMenuData(funId, licenseBh);
                        List<FunModel> funList = functionsList(new ArrayList<>(), funModelList, funId);
                        if(PlaceholderUtil.judgeCollect(funList)){
                            funList.stream().forEach(funModel -> {
                                List<PatternModel> patternModelList = funModel.getPatterns();
                                if(PlaceholderUtil.judgeCollect(patternModelList)){
                                    patternModelList.stream().forEach(patternModel -> {
                                        String url = patternModel.getMethod().toUpperCase() +" "+ patternModel.getPath();
                                        // 操作控制项映射
                                        assembleOperateData(url, licenseBh, funId);
                                    });
                                }
                            });
                        }
                    });
                }
                //解析本身子级license
                List<LicenseModel> licenseList = licenseModel.getLicenses();
                if(PlaceholderUtil.judgeCollect(licenseList)){
                    licenseAnalysis(licenseList, funModelList);
                }
            });
        }
    }

    //组装license控制项数据
    private static void assembleLicenseData(LicenseModel licenseModel) {
        String id = licenseModel.getId();
        String content = licenseModel.getTitle();
        Boolean basic = licenseModel.getBasic();
        String dependency = licenseModel.getDependency();
        String value = licenseModel.getValue();
        String parentId = licenseModel.getParent();
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("content", content);
        params.put("basic", basic);
        params.put("dependency", dependency);
        params.put("value", value);
        params.put("parent", parentId);
        if(!licenseControlList.contains(params)){
            licenseControlList.add(params);
        }
    }

    //组装菜单控制项数据
    private static void assembleMenuData(String funId, String licenseBh) {
        JSONObject params = new JSONObject();
        params.put("menu", funId);
        params.put("id", licenseBh);
        if(!menuControlList.contains(params)){
            menuControlList.add(params);
        }
    }

    //组装操作控制项数据
    private static void assembleOperateData(String pattern, String licenseBh, String funId) {
        JSONObject params = new JSONObject();
        params.put("operate", pattern);
        params.put("id", licenseBh);
        params.put("remark", funId);
        if(!operateControlList.contains(params)){
            operateControlList.add(params);
        }
    }

    /**
     * 根据funId获取匹配的functions
     * @param funModelsResult
     * @param functions
     * @param funId
     * @return
     */
    public static List<FunModel> functionsList(List<FunModel> funModelsResult, List<FunModel> functions, String funId) {
        List<FunModel> funModelsBack = funModelsResult;
        if(PlaceholderUtil.judgeCollect(functions)){
            functions.stream().forEach(funModel -> {
                String dependencyFunId = funModel.getId();
                if(dependencyFunId.equalsIgnoreCase(funId)){
                    funModelsBack.add(funModel);
                }else{
                    //获取子级
                    List<FunModel> funModelChild = funModel.getFunctions();
                    if(PlaceholderUtil.judgeCollect(funModelChild)){
                        functionsList(funModelsBack, funModelChild, funId);
                    }
                }
            });
        }
        return funModelsBack;
    }

    /**
     * 写入权限文件
     * @param fileName
     * @throws IOException
     */
    public static void writeOpsToFile(String fileName) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(fileName));
        out.println("USE baas;");
        out.println("delete from auth_op;");
        out.println("delete from auth_op_pattern;");
        out.println("delete from auth_role_op_default;");
        out.println("commit;");
        // 基本权限
        comment("BaaS init operations", out);
        if(PlaceholderUtil.judgeCollect(opSqlList)){
            opSqlList.stream().forEach(url -> out.println(url));
        }
        // 默认角色权限
        comment("BaaS default role operations", out);
        if(PlaceholderUtil.judgeCollect(roleOpSqlList)){
            roleOpSqlList.stream().forEach(url -> out.println(url));
        }
        // 权限和URL映射关系
        comment("BaaS pattern relations", out);
        if(PlaceholderUtil.judgeCollect(patternOpSqlList)){
            patternOpSqlList.stream().forEach(url -> out.println(url));
        }

        IoUtil.close(out);
    }

    /**
     * 写入license文件
     * @param fileName
     * @throws IOException
     * @throws ParseException
     */
    public static void writeLicenseToFile(String fileName) throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        json.put("companyName", "测试公司");
        json.put("authorizationType", "通用");
        json.put("expiryDate", sdf.format(sdf.parse("2021/12/31")));
        json.put("license", licenseControlList);
        jsonObject.put("license", json);
        jsonObject.put("menu", menuControlList);
        jsonObject.put("operate", operateControlList);

        PrintWriter out = new PrintWriter(new FileWriter(fileName));
        out.println(jsonObject);

        IoUtil.close(out);
    }

}
