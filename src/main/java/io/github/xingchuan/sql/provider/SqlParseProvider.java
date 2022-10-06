package io.github.xingchuan.sql.provider;

import cn.hutool.json.JSONObject;

/**
 * sql转换的提供类
 *
 * @author xingchuan.qxc
 * @date 2022/10/6
 */
public interface SqlParseProvider {

    /**
     * 转换sql
     *
     * @param template
     * @param params
     * @return
     */
    String parseSql(String template, JSONObject params);

    /**
     * 该provider的类型，在同一个应用内，应该唯一
     *
     * @return
     */
    String type();
}
