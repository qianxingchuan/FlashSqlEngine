package io.github.xingchuan.sql.provider.impl.mybatis.xmltags;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * @author Wen
 */
public class SqlMeta {


    private String sql;

    private List<Object> parameter;

    public SqlMeta(String sql, List<Object> parameter) {
        super();
        this.sql = sql;
        this.parameter = parameter;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getParameter() {
        return parameter;
    }

    public void setParameter(List<Object> parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return "SqlInfo [sql=" + sql + ", parameter=" + parameter + "]";
    }

    public String fetchTargetSql() {
        String sql = this.sql;
        List<Object> parameter = getParameter();
        for (Object v : parameter) {
            String parameterValue = getParameterValue(v);
            sql = StrUtil.subBefore(sql, "?", false) + parameterValue + StrUtil.subAfter(sql, "?", false);
        }
        return sql;
    }

    /**
     * 获得参数值
     *
     * @param param
     * @return
     */
    private String getParameterValue(Object param) {
        String value = null;
        if (param == null) {
            return null;
        }
        if (param instanceof Number) {
            value = param.toString();
        } else if (param instanceof String) {
            value = "'" + param + "'";
        } else if (param instanceof Date) {
            value = "'" + DateUtil.format((Date) param, DatePattern.NORM_DATETIME_FORMAT) + "'";
        } else if (param instanceof Enum) {
            value = "'" + ((Enum<?>) param).name() + "'";
        } else if (param instanceof JSONObject) {
            JSONObject paramJson = (JSONObject) param;
            String jsonStr = paramJson.toString();
            jsonStr = StrUtil.replace(jsonStr, "\\", "\\\\");
            value = "'" + jsonStr + "'";
        } else {
            value = "'" + param + "'";
        }

        return value;
    }


}
