package io.github.xingchuan.sql.provider.impl;

import cn.hutool.json.JSONObject;
import io.github.xingchuan.sql.provider.SqlParseProvider;
import io.github.xingchuan.sql.provider.impl.mybatis.xmltags.Configuration;
import io.github.xingchuan.sql.provider.impl.mybatis.xmltags.SqlMeta;
import io.github.xingchuan.sql.provider.impl.mybatis.xmltags.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的sql模板转换（兼容MyBatis的模板内容）
 *
 * @author xingchuan.qxc
 * @date 2022/10/6
 */
public class DefaultMybatisSqlParseProvider implements SqlParseProvider {

    private Logger logger = LoggerFactory.getLogger(DefaultMybatisSqlParseProvider.class);

    public static final String MYBATIS_SQL_TYPE = "MYBATIS_SQL_TYPE";

    private final Configuration configuration = new Configuration();

    @Override
    public String parseSql(String template, JSONObject params) {
        SqlTemplate templateProcess = configuration
                .getTemplate(template);
        SqlMeta process = templateProcess.process(params);
        return process.fetchTargetSql();
    }


    @Override
    public String type() {
        return MYBATIS_SQL_TYPE;
    }
}
