package com.parsing;

import com.parsing.model.Const;
import com.parsing.model.FunModel;
import com.parsing.model.LicenseModel;
import com.parsing.model.ServiceModel;
import com.parsing.utils.AnalysisUtil;
import com.parsing.utils.GitLabAPIUtil;
import com.parsing.utils.YamlToolUtil;
import org.apache.http.Consts;

import java.util.ArrayList;
import java.util.List;

/**
 * main 入口
 * @author wugang
 * @since 2021-12-9
 */
public class Main {
    public static void main( String[] args ) throws Exception {
        /** 第一种方式：根据module.yaml路径读取并解析文件 */
        YamlToolUtil tools = new YamlToolUtil("D:\\module.yaml");
        /** 第二种方式：读取gitlab代码库文件，返回文件解析内容；以及转成配置对象 */
//        String content = GitLabAPIUtil.getFileContentFromRepository(Const.REPO_IP, Const.PROJECT_PATH, Const.USERNAME, Const.PASSWORD, Const.FILE_PATH,Const.BRANCH_NAME);
//        YamlToolUtil tools = new YamlToolUtil();
//        tools.initWithString(content);
        System.out.println(tools.getServiceModel());
        /** 解析文件 */
        // 1、解析functions
        AnalysisUtil.functionsAnalysis(tools.getServiceModel().getFunctions());
        // 2、解析license
        AnalysisUtil.licenseAnalysis(tools.getServiceModel().getLicenses(), tools.getServiceModel().getFunctions());
        /** 写入文件 */
        // 1、写入权限文件
        AnalysisUtil.writeOpsToFile(Const.auth_file_name);
        // 2、写入license文件
        AnalysisUtil.writeLicenseToFile(Const.license_file_name);

//        tools.writeBack("D:\\workspace\\module.yaml", tools.getServiceModel());
//        tools.writeFile("D:\\workspace\\chainswagger.json", "D:\\workspace\\chainswagger.yaml");

    }

}
