package io.github.xingchuan.sql.engine;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.xingchuan.sql.provider.impl.DefaultMybatisSqlParseProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.github.xingchuan.sql.provider.impl.DefaultMybatisSqlParseProvider.MYBATIS_SQL_TYPE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author xingchuan.qxc
 * @since 1.0
 */
public class FlashSqlEngineTest {

    private Logger logger = LoggerFactory.getLogger(FlashSqlEngineTest.class);

    @Test
    public void test_using_templateContent_render_sql() {
        JSONObject param = JSONUtil.createObj();
        param.set("userCode", "xingchuan.qxc");
        param.set("userName", "钱幸川");
        FlashSqlEngine flashSqlEngine = new FlashSqlEngine();
        flashSqlEngine.registerSqlParseProvider(MYBATIS_SQL_TYPE, new DefaultMybatisSqlParseProvider());
        String sqlTemplate = "select * from t_user\n" +
                "        where tenant_code ='xingchuan'\n" +
                "        <if test=\"userCode != null\">\n" +
                "            and user_code = #{userCode}\n" +
                "        </if>\n" +
                "        <if test=\"userName != null\">\n" +
                "            and user_name = #{userName}\n" +
                "        </if>";
        String sql = flashSqlEngine.parseSql(sqlTemplate, param, MYBATIS_SQL_TYPE);
        logger.info("{}", sql);
    }

    @Test
    public void test_loadConfig_parseTemplateToSql() throws IOException {
        FlashSqlEngine flashSqlEngine = new FlashSqlEngine();
        flashSqlEngine.registerSqlParseProvider(MYBATIS_SQL_TYPE, new DefaultMybatisSqlParseProvider());
        flashSqlEngine.loadConfig("test-sql-mapper.xml");

        JSONObject param = JSONUtil.createObj();
        param.set("userCode", "xingchuan.qxc");
        param.set("userName", "钱幸川");
        String testQuerySql = flashSqlEngine.parseSqlWithSqlId("testQuery", param);
        logger.info("{}", testQuerySql);

        assertThat(testQuerySql).isEqualTo("\n" +
                "        select * from t_user\n" +
                "        where tenant_code ='xingchuan'\n" +
                "         \n" +
                "            and user_code = 'xingchuan.qxc'\n" +
                "         \n" +
                "         \n" +
                "            and user_name = '钱幸川'\n" +
                "         \n" +
                "     ");

        // 测试sql注入， 这边是存在sql注入的，所以，本框架不能用作鉴权类相关的服务
        //select * from t_user where code = #{code} and password = #{password}
        JSONObject param2 = JSONUtil.createObj();
        param2.set("code", "xingchuan.qxc");
        param2.set("password", "' or 1 = 1 or '' = '");
        String testLoginSql = flashSqlEngine.parseSqlWithSqlId("testLogin", param2);
        logger.info("{}", testLoginSql);
        assertThat(testLoginSql).isEqualTo("\n" +
                "        select * from t_user where code = 'xingchuan.qxc' and password = '' or 1 = 1 or '' = ''\n" +
                "     ");

        // 更新测试
        JSONObject param3 = JSONUtil.createObj();
        param3.set("code", "xingchuan.qxc");
        param3.set("name", "钱幸川");
        param3.set("tenantCode", "xingchuan");
        JSONObject param3_update = JSONUtil.createObj();
        param3_update.set("user", param3);
        String testUpdateSql = flashSqlEngine.parseSqlWithSqlId("testUpdate", param3_update);
        logger.info("{}", testUpdateSql);
        assertThat(testUpdateSql).isEqualTo("\n" +
                "        update t_user\n" +
                "        set gmt_modified = now(),\n" +
                "            user_name    = '钱幸川'\n" +
                "        where tenant_code = 'xingchuan'\n" +
                "          and user_code = 'xingchuan.qxc'\n" +
                "     ");

        //insert测试
        JSONArray array = JSONUtil.createArray();
        array.add(param3);
        array.add(param3);
        JSONObject param4 = JSONUtil.createObj();
        param4.set("userList", array);
        String testInsertSql = flashSqlEngine.parseSqlWithSqlId("testInsert", param4);
        logger.info("{}", testInsertSql);
        assertThat(testInsertSql).isEqualTo("\n" +
                "        insert into t_user\n" +
                "        (gmt_create,gmt_modified,user_code,user_name,tenant_code)\n" +
                "        values\n" +
                "          \n" +
                "            (now(),now(),'xingchuan.qxc','钱幸川','xingchuan')\n" +
                "         , \n" +
                "            (now(),now(),'xingchuan.qxc','钱幸川','xingchuan')\n" +
                "         \n" +
                "     ");
    }
}