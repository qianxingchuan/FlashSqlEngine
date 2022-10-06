package io.github.xingchuan.sql.provider.impl.mybatis.xmltags.script;

import io.github.xingchuan.sql.provider.impl.mybatis.xmltags.Context;


/**
 * @author Wen
 */
public interface SqlFragment {
    boolean apply(Context context);

}
