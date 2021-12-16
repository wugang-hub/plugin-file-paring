package com.parsing.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.parsing.model.*;
import org.apache.http.Consts;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.*;
import java.util.Map;

/**
 * Yaml 解析工具类
 * @author wugang
 * @since 2021-12-9
 */
public class YamlToolUtil {
    //初始化定义Yaml
    private Yaml yaml = new Yaml();

    // yaml文件内容解析的对象模型
    public ServiceModel serviceModel;

    public ServiceModel getServiceModel() {
        return this.serviceModel;
    }

    public void setServiceModel(ServiceModel serviceModel){
        this.serviceModel = serviceModel;
    }

    // 空的构造函数
    public YamlToolUtil() {}

    // 以文件路径为条件的构造函数
    public YamlToolUtil(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setServiceModel(yaml.loadAs(inputStream, ServiceModel.class));
    }

    /**
     * 从String 中获取配置转成 ServiceModel 对象
     * @param content
     */
    public void initWithString(String content) {
        setServiceModel(yaml.loadAs(content, ServiceModel.class));
    }

    /**
     * 将内容写成 yaml文件
     * @param path
     * @param serviceModel
     * @throws FileNotFoundException
     */
    public void writeBack(String path, ServiceModel serviceModel) throws FileNotFoundException {
        File file=new File(path);
        PrintWriter out = new PrintWriter(file);
        String output = yaml.dumpAsMap(serviceModel);
        out.println(output);
        //关闭写出器
        out.close();
    }

    /**
     * 文件内容格式转换
     * @param resFilePath
     * @param desFilePath
     * @throws IOException
     */
    public void writeFile(String resFilePath, String desFilePath) throws IOException {
        File resFile = new File(resFilePath);
        Reader reader = new InputStreamReader(new FileInputStream(resFile), Consts.UTF_8);
        Map<String, Object> map = yaml.load(reader);
        String output = yaml.dumpAsMap(map);
        File desFile = new File(desFilePath);
        PrintWriter out = new PrintWriter(desFile);
        out.println(output);
        out.close();
    }

}
