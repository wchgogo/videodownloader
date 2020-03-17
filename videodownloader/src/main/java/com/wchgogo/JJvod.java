package com.wchgogo;

import com.wchgogo.utils.HttpClientUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JJvod {
    public static void main(String[] args) throws Exception {
        String originUrl = "https://www.jjvcd.com/v/556-1-1.html";
        String url = parseIndexM3U8(originUrl);
        System.out.println("url " + url);
        String name = parseName(originUrl);
        System.out.println("片名《" + name + "》");
        new Downloader(url, name, "C:\\Users/mengw/Desktop/Download", 10, false, null).start();
    }

    private static String parseIndexM3U8(String originUrl) throws Exception {
        String html = HttpClientUtil.httpGet(originUrl);
        Pattern pattern = Pattern.compile("https.*\\.m3u8");
        Matcher matcher = pattern.matcher(html);
        matcher.find();
        return matcher.group().replace("\\", "");
    }

    private static String parseName(String originUrl) throws Exception {
        String html = HttpClientUtil.httpGet(originUrl);
        Pattern pattern = Pattern.compile("<title>.*《(.*)》.*</title>");
        Matcher matcher = pattern.matcher(html);
        matcher.find();
        return matcher.group(1);
    }
}