package io.github.xingchuan.sql.engine;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import io.github.xingchuan.sql.provider.SqlParseProvider;
import io.github.xingchuan.sql.xml.XmlDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static io.github.xingchuan.sql.provider.impl.DefaultMybatisSqlParseProvider.MYBATIS_SQL_TYPE;


/**
 * 模板的引擎类，用于根据模板生成sql内容
 *
 * @author xingchuan.qxc
 * @since 1.0
 */
public class FlashSqlEngine {

    private Logger logger = LoggerFactory.getLogger(FlashSqlEngine.class);

    /**
     * 从配置文件读出来的sqlId映射，key -> sqlId value -> sql模板内容
     */
    private Map<String, String> sqlIdMap = new HashMap<>();


    /**
     * sql转换内容的提供类
     */
    private Map<String, SqlParseProvider> sqlParseProviderMap = new HashMap();

    /**
     * 注册一个sql转换类型
     *
     * @param typeCode sql转换器类型code
     * @param provider sql转换器对象
     */
    public void registerSqlParseProvider(String typeCode, SqlParseProvider provider) {
        this.sqlParseProviderMap.put(typeCode, provider);
        logger.info("sqlParseProvider {} registered ok.", typeCode);
    }

    /**
     * 根据configFilePath，初始化内容
     *
     * @param configFilePath 待加载的资源位置
     * @throws IOException
     */
    public void loadConfig(String configFilePath) throws IOException {
        try (InputStream inputStream = loadConfigSourceStream(configFilePath)) {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            sqlIdMap.putAll(XmlDocumentParser.fetchXmlDocumentSql(document, "select"));
            sqlIdMap.putAll(XmlDocumentParser.fetchXmlDocumentSql(document, "update"));
            sqlIdMap.putAll(XmlDocumentParser.fetchXmlDocumentSql(document, "insert"));
            sqlIdMap.putAll(XmlDocumentParser.fetchXmlDocumentSql(document, "delete"));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 转换sqlId 的内容成为可执行的sql
     *
     * @param sqlId  配置文件中的sqlId
     * @param params 构建的参数Json对象
     * @return 渲染完成的sql
     */
    public String parseSqlWithSqlId(String sqlId, JSONObject params) {
        return parseSqlWithSqlId(sqlId, params, MYBATIS_SQL_TYPE);
    }

    /**
     * 转换sqlId 的内容成为可执行的sql
     *
     * @param sqlId        配置文件中的sqlId
     * @param params       构建的参数Json对象
     * @param providerType 转换器类型code
     * @return 渲染完成的sql
     */
    public String parseSqlWithSqlId(String sqlId, JSONObject params, String providerType) {
        if (StrUtil.isBlank(sqlId)) {
            return StrUtil.EMPTY;
        }
        String sqlTemplateContent = sqlIdMap.get(sqlId);
        if (StrUtil.isBlank(sqlTemplateContent)) {
            return StrUtil.EMPTY;
        }
        return parseSql(sqlTemplateContent, params, providerType);
    }

    /**
     * 转换成为可以执行的sql
     *
     * @param template 模板内容
     * @param params   构建的参数JSON对象
     * @return 渲染完成的sql
     */
    public String parseSql(String template, JSONObject params, String providerType) {
        SqlParseProvider sqlParseProvider = sqlParseProviderMap.get(providerType);
        if (sqlParseProvider == null) {
            throw new IllegalArgumentException("provider " + providerType + " not found");
        }
        return sqlParseProvider.parseSql(template, params);
    }


    /**
     * 将path转换成对应的字节流
     *
     * @param configFilePath 配置文件的位置
     * @return 对应的字节输入流
     */
    private InputStream loadConfigSourceStream(String configFilePath) {
        File configFile = FileUtil.file(configFilePath);
        boolean exist = FileUtil.exist(configFile);
        if (!exist) {
            // 不是一个标准文件，从类路径下获取一次
            return ResourceUtil.getStream(configFilePath);
        }
        return IoUtil.toStream(FileUtil.readBytes(configFile));
    }
}
