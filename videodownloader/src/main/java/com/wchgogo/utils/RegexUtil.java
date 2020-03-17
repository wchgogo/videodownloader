package com.wchgogo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    public static String find(String str, String patternStr, int group) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(group);
        }
        System.err.println("can not find " + patternStr + " in " + str);
        return null;
    }

    public static List<String> findAll(String str, String patternStr, int group) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(str);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group(group));
        }
        return list;
    }
}