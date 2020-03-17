package com.wchgogo.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtil {
    public static String getProtocolHost(String urlStr) throws MalformedURLException {
        if (urlStr.contains("?")) {
            urlStr = urlStr.substring(0, urlStr.indexOf("?"));
        }
        URL url = new URL(urlStr);
        return url.getProtocol() + "://" + url.getHost();
    }

    public static String getLastPath(String urlStr) throws MalformedURLException {
        if (urlStr.contains("?")) {
            urlStr = urlStr.substring(0, urlStr.indexOf("?"));
        }
        if (urlStr.endsWith("/")) {
            urlStr = urlStr.substring(0, urlStr.length() - 1);
        }
        URL url = new URL(urlStr);
        return url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
    }
}