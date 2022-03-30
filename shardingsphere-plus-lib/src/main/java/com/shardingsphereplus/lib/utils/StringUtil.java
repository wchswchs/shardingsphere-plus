package com.shardingsphereplus.lib.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static String subString(String str, int startIndex, int endIndex) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        if (startIndex < -1 || endIndex < -1) {
            throw new IllegalArgumentException("startIndex and endIndex can not lower than -1");
        }
        if (startIndex == -1 && endIndex == -1) {
            return str;
        }
        if (startIndex >= 0 && endIndex == -1) {
            startIndex = 0;
            endIndex = startIndex;
        }
        if (startIndex == -1 && endIndex >= 0) {
            startIndex = str.length() - endIndex;
            endIndex = str.length();
        }
        return StringUtils.substring(str, startIndex, endIndex);
    }

}
