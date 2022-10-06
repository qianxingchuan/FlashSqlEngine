# Flash Sql Engine
## Background

在2019年的时候，由于人力成本等因素，项目组没有前端人员，所以我们需要自己开发报表（react + fusion + 后端接口），
开始两周，大家都在重复同样的工作( 写SQL -> 写接口 -> 写页面)。

我希望有更多的时间做自己的事情，于是我尝试把这个过程进行抽象，可以自动的生成我们需要的报表页面。

最终，我完成了一个FlashQuery的框架，这个框架只需要写Sql模板（可以任意模板，但是为了降低学习成本，默认支持MyBatis的xml模板格式），模板中的参数就是前端的入参，整个过程抽象成了
（前端传参 -> 接收到JsonObject -> 渲染sql -> 执行sql -> 返回前端组件认识的格式 -> 报表展现)

FlashSqlEngine，本质就是一个模板引擎，用来渲染出来一个可执行的Sql的。

本框架引用另一个github的项目 https://github.com/wenzuojing/SqlTemplate ，在其基础上做了一些扩展，修复了一些已知问题。

## How to use ?

### maven 依赖

```
     
        <dependency>
            <groupId>io.github.flash-query</groupId>
            <artifactId>FlashSqlEngine</artifactId>
            <version>1.0</version>
        </dependency>

```

### 场景1：
#### 1、 在类路径下创建一个配置文件 ` my-1st-mapper.xml ` ，比如位置在 ` resources ` 

```

        <?xml version="1.0" encoding="UTF-8" ?>
        <!DOCTYPE mapper
                PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
                "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
        <mapper namespace="">
        
            <select id="testLogin">
                select * from t_user where code = #{code} and password = #{password}
            </select>
            <select id="testQuery">
                select * from t_user
                where tenant_code ='xingchuan'
                <if test="userCode != null">
                    and user_code = #{userCode}
                </if>
                <if test="userName != null">
                    and user_name = #{userName}
                </if>
            </select>
            <insert id="testInsert">
                insert into t_user
                (gmt_create,gmt_modified,user_code,user_name,tenant_code)
                values
                <foreach collection="userList" item="user" separator=",">
                    (now(),now(),#{user.code},#{user.name},#{user.tenantCode})
                </foreach>
            </insert>
            <update id="testUpdate">
                update t_user
                set gmt_modified = now(),
                    user_name    = #{user.name}
                where tenant_code = #{user.tenantCode}
                  and user_code = #{user.code}
            </update>
            <delete id="testDelete">
                delete t_user
                where tenant_code = #{user.tenantCode}
                and user_code = #{user.code}
            </delete>
        </mapper> 

```

#### 2、 初始化FlashSqlEngine

```

    FlashSqlEngine flashSqlEngine = new FlashSqlEngine();
    // 提供了默认的mybatis的渲染机制，如果需要别的，只需要提供对应的sqlParseProvider即可
    flashSqlEngine.registerSqlParseProvider(MYBATIS_SQL_TYPE, new DefaultMybatisSqlParseProvider());
    flashSqlEngine.loadConfig("my-1st-mapper.xml");

```

#### 3、 获得sqlId，填入参数，渲染sql

```
        
        // 构造参数    
        JSONObject param = JSONUtil.createObj();
        param.set("userCode", "xingchuan.qxc");
        param.set("userName", "钱幸川");
        // 渲染sql
        String testQuerySql = flashSqlEngine.parseSqlWithSqlId("testQuery", param);
        // 结果是 select * from t_user where tenant_code = 'xingchuan' and user_code ='xingchuan.qxc' and user_name ='钱幸川'
        logger.info("{}", testQuerySql);

```

### 场景2：直接载入对应的sql模板，进行渲染


```

    // 构造参数
    JSONObject param = JSONUtil.createObj();
    param.set("userCode", "xingchuan.qxc");
    param.set("userName", "钱幸川");
    // 初始化，如果是spring 相关的项目，engine只需要初始化一次
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
    // 目标结果是 select * from t_user where tenant_code = 'xingchuan' and user_code ='xingchuan.qxc' and user_name = '钱幸川'
    logger.info("{}", sql);
    

```


## Warning

**该框架只用来生成sql，基于报表查询场景，生成出来的sql没有处理sql注入等安全问题。**