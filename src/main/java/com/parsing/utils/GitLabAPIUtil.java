package com.parsing.utils;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.parsing.model.Const;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * gitlab api 工具类
 * @author wugang
 * @since 2021-12-9
 */
public class GitLabAPIUtil {

    /** 获取gitlab的private token */
    private static String GITLAB_SESSION_API = "https://#{REPO_IP}/api/v4/session/login=wugang&password=wugang123";

    /** 获取指定项目的projectId */
    private static String GITLAB_SINGLE_PROJECT_API = "https://#{REPO_IP}/api/v4/projects/#{PROJECT_PATH}?private_token=#{PRIVATE_TOKEN}";

    /** 获取gitlab的文件内容 */
    private static String GITLAB_FILE_CONTENT_API = "https://#{REPO_IP}/api/v4/projects/#{PROJECT_ID}/repository/files/#{FILE_PATH}?ref=#{BRANCH_NAME}";

    /**
     * 根据用户名称和密码获取gitlab的private token，为Post请求
     * @param ip    gitlab仓库的ip
     * @param userName  登陆gitlab的用户名
     * @param password  登陆gitlab的密码
     * @return  返回该用户的private_token
     */
    public static String getPrivateTokenByPassword(String ip, String userName, String password) {
        //  校验参数
        Objects.requireNonNull(ip, "参数ip不能为空！");
        Objects.requireNonNull(userName, "参数userName不能为空！");
        Objects.requireNonNull(password, "参数password不能为空！");
        //  参数准备，存入map
        Map<String, Object> params = new HashMap<>(4);
        params.put("REPO_IP", ip);
//        params.put("USER_NAME", userName);
//        params.put("PASSWORD", password);
        /** 1.参数替换，生成获取指定用户privateToken地址 */
        String reqUserTokenUrl = PlaceholderUtil.anotherReplace(GITLAB_SESSION_API, params);
        /** 2.访问url，获取指定用户的信息 */
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(reqUserTokenUrl, null, String.class);
        /** 3.解析结果 */
        String body = response.getBody();
        JSONObject jsonBody = JSON.parseObject(body);
        String privateToken =jsonBody.getString("private_token");
        /** 4.返回privateToken */
        return privateToken;
    }

    /**
     * 使用gitLab api获取指定项目的projectId，为Get请求
     * @param ip  项目仓库的ip地址
     * @param projectPath   项目的path
     * @param privateToken  访问gitlab库时的privateToken
     * @return  返回目的projectId
     */
    public static String getProjectId(String ip, String projectPath, String privateToken) {
        /** 1.参数替换，生成访问获取project信息的uri地址 */
        //  校验参数
        Objects.requireNonNull(ip, "参数ip不能为空！");
        Objects.requireNonNull(projectPath, "参数projectPath不能为空！");
        Objects.requireNonNull(privateToken, "参数privateToken不能为空！");
        //  参数准备，存入map
        Map<String, Object> params = new HashMap<>(4);
        params.put("REPO_IP", ip);
        params.put("PRIVATE_TOKEN", privateToken);
        // gitlab api要求项目的path需要安装uri编码格式进行编码，比如"/"编码为"%2F"
        try {
            params.put("PROJECT_PATH", URLEncoder.encode(projectPath, String.valueOf(Consts.UTF_8)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(String.format("对%s进行URI编码出错！", projectPath));
        }
        // 调用工具类替换,得到具体的调用地址
        String getSingleProjectUrl = PlaceholderUtil.anotherReplace(GITLAB_SINGLE_PROJECT_API, params);
        //  创建URI对象
        URI url = null;
        try {
            url = new URI(getSingleProjectUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("使用%s创建URI出错！", getSingleProjectUrl));
        }
        /** 2.访问url，获取制定project的信息 */
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        /** 3.解析结果 */
        if (result.getStatusCode() != HttpStatus.OK ) {
            throw new RuntimeException(String.format("请求%s出错！错误码为：%s", url, result.getStatusCode()));
        }
        JSONObject responseBody = JSON.parseObject(result.getBody());
        String projectId = responseBody.getString("id");
        /** 4.返回projectId */
        return projectId;
    }

    public static String getFileContentFromRepository(String ip, String projectPath, String userName, String password, String fileFullPath, String branchName) throws Exception {
        //  校验参数
        Objects.requireNonNull(ip, "参数ip不能为空！");
        Objects.requireNonNull(projectPath, "参数projectPath不能为空！");
//        Objects.requireNonNull(userName, "参数userName不能为空！");
//        Objects.requireNonNull(password, "参数password不能为空！");
        Objects.requireNonNull(fileFullPath, "参数fileFullPath不能为空！");
        Objects.requireNonNull(branchName, "参数branchName不能为空！");
        /** 1.依据用户名、密码获取到用户的privateToken */
//        String privateToken = getPrivateTokenByPassword(ip, userName, password);
        String privateToken = Const.PRIVATE_TOKEN;
        /** 2.使用privateToken获取项目的projectId */
        String projectId = getProjectId(ip, projectPath, privateToken);
        /** 3.使用参数替换形成请求git库中文件内容的uri */
        //  参数准备，存入map
        Map<String, Object> params = new HashMap<>();
        params.put("REPO_IP", ip);
        params.put("PROJECT_ID", projectId);
        params.put("PRIVATE_TOKEN", privateToken);
        params.put("FILE_PATH", URLEncoder.encode(fileFullPath, String.valueOf(Consts.UTF_8)));
        params.put("BRANCH_NAME", branchName);
        //  使用工具类替换参数
        String reqFileContentUri = PlaceholderUtil.anotherReplace(GITLAB_FILE_CONTENT_API, params);
        /** 4.请求gitlab获取文件内容 */
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("PRIVATE-TOKEN", privateToken);
        //RestTemplate 会再次对 URL 进行编码, 可以使用方法来判断URI已经编码
        URI gitlabUri = UriComponentsBuilder.fromHttpUrl(reqFileContentUri).build(true).toUri();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(gitlabUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
        /** 5.解析响应结果内容 */
        String body = response.getBody();
        JSONObject jsonBody = JSON.parseObject(body);
        String content = new String(Base64.decode(jsonBody.getString("content")), Consts.UTF_8);
        /** 6.返回内容 */
        return content;
    }

    public static void getToken() throws IOException {
        CloseableHttpClient httpClients = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://gitlab.33.cn/api/v4/users/315/impersonation_tokens");
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("name", "wugang"));
        formParams.add(new BasicNameValuePair("expires_at", "2022-04-04"));
        formParams.add(new BasicNameValuePair("scopes", "api"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = httpClients.execute(httpPost);
        org.apache.http.HttpEntity entity1 = response.getEntity();
        String aa = EntityUtils.toString(entity1);
        System.out.println(aa);
    }

}
