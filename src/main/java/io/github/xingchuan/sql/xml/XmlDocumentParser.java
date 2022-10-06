package io.github.xingchuan.sql.xml;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取xml的文件配置
 *
 * @author xingchuan.qxc
 * @since 1.0
 */
public class XmlDocumentParser {

    private static Logger logger = LoggerFactory.getLogger(XmlDocumentParser.class);

    /**
     * 读取xml document指定类型的sql id的map
     *
     * @param document xml doc对象
     * @param type     select ? update ? delete? insert?
     * @return key: sqlId value: sql Template Content
     */
    public static Map<String, String> fetchXmlDocumentSql(Document document, String type) {
        Map<String, String> resultSqlIdMap = new HashMap<>();
        NodeList nodeList = document.getElementsByTagName(type);
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node item = nodeList.item(i);
            String id = item.getAttributes().getNamedItem("id").getNodeValue();
            if (StrUtil.isBlank(id)) {
                continue;
            }
            StringBuilder templateBuilder = new StringBuilder();
            NodeList childNodes = item.getChildNodes();
            int childNodesLength = childNodes.getLength();
            for (int j = 0; j < childNodesLength; j++) {
                Node childNode = childNodes.item(j);
                String templateContent = xmlNodeToString(childNode);
                templateBuilder.append(templateContent);
            }
            resultSqlIdMap.put(id, templateBuilder.toString());
            logger.info("sqlId {} loaded to cache.", id);

        }
        return resultSqlIdMap;
    }

    /**
     * 将传入的一个Dom Node对象输出为字符串，如果失败，返回一个空字符串
     *
     * @param node Dom node 对象
     * @return a xml string from node
     */
    private static String xmlNodeToString(Node node) {
        Transformer transformer = null;
        if (node == null) {
            throw new IllegalArgumentException();
        }
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (transformer != null) {
            try {
                StringWriter sw = new StringWriter();
                transformer.transform(new DOMSource(node), new StreamResult(sw));
                String content = sw.toString();
                content = StrUtil.replace(content, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
                return content;
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
