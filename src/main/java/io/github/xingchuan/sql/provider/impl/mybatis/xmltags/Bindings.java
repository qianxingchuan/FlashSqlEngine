package io.github.xingchuan.sql.provider.impl.mybatis.xmltags;

import java.util.HashMap;

public class Bindings extends HashMap<Object,Object>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7290846439659491933L;

	public Bindings bind(Object key , Object value ){
		this.put(key, value) ;
		
		return this; 
	}

}
