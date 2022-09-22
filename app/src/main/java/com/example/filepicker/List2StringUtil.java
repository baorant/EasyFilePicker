package com.example.filepicker;

import java.util.List;

public class List2StringUtil {
    public static String combine(List list) {
        if (list == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Object object : list) {
            if (object != null) {
                result.append(object.toString()).append(",");
            }
        }
        if (result.length() > 0) {
            result = new StringBuilder(result.substring(0, result.length() - 1));
        }

        return result.toString();
    }
}
