package com.whs.whsapi.util;

import org.apache.commons.lang3.StringUtils;

public class EnvUtil {

	public static String getEnvValue( String keyEnv ) {
		String ret = null;
		ret = System.getProperty(keyEnv);
		if( StringUtils.isBlank(ret) )
			ret = System.getenv(keyEnv);
		return ret;
	}
}
