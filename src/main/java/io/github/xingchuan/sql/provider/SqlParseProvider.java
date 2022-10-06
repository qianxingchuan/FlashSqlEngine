package io.github.xingchuan.sql.provider;

import cn.hutool.json.JSONObject;

/**
 * sql转换的提供类
 *
 * @author xingchuan.qxc
 * @since 1.0
 */
public interface SqlParseProvider {

    /**
     * 转换sql
     *
     * @param template 模板内容
     * @param params   参数JSON对象
     * @return 渲染完成的sql
     */
    String parseSql(String template, JSONObject params);

    /**
     * 该provider的类型，在同一个应用内，应该唯一
     *
     * @return 转换器类型code
     */
    String type();
}
