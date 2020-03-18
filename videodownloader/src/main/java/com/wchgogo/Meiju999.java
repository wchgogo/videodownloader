package com.wchgogo;

import com.wchgogo.utils.HttpClientUtil;
import com.wchgogo.utils.JsUtil;
import com.wchgogo.utils.RegexUtil;
import com.wchgogo.utils.UrlUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Meiju999 {
    public static void main(String[] args) throws Exception {
        String rootPath = "D:/Download/Videos";
        String episodeUrl = "https://www.999meiju.com/vodplay/2192-1-1.html";
        String seasonUrl = "https://www.999meiju.com/vod/xibushijiedierji/";
        String outFormat = "mp4";

//        downloadEpisode(episodeUrl, rootPath, true);
        downloadSeason(seasonUrl, rootPath, outFormat);
    }

    public static void downloadSeason(String seasonUrl, String rootPath, String outFormat) throws Exception {
        String host = UrlUtil.getProtocolHost(seasonUrl);
        String html = HttpClientUtil.httpGet(seasonUrl);
        String data = RegexUtil.find(html, "<div class=\"mlist scroll\">.*?</div>", 0);
        List<String> list = RegexUtil.findAll(data, "\"(/.*?\\.html)\"", 1);
        String seasonName = UrlUtil.getLastPath(seasonUrl);
        System.out.println(seasonName);
        File file = getDownloadRecord(rootPath, seasonName);
        if (file == null) return;
        List<String> completeList = IOUtils.readLines(new FileReader(file));
        FileWriter writer = new FileWriter(file, true);
        for (String item : list) {
            String episodeUrl = host + item;
            if (completeList.contains(item)) {
                System.out.println("跳过 " + episodeUrl);
                continue;
            }
            System.out.println("正在下载 " + episodeUrl);
            try {
                downloadEpisode(episodeUrl, rootPath, true, outFormat);
                writer.write(item + "\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        writer.close();
    }

    public static void downloadEpisode(String episodeUrl, String rootPath, boolean purge, String outFormat) throws Exception {
        String m3u8Url = parseIndexM3U8(episodeUrl);
        String name = parseName(episodeUrl);

        if (episodeUrl != null && name != null) {
            System.out.println("片名《" + name + "》 " + "url " + episodeUrl);
            new Downloader(m3u8Url, name, rootPath, 10, purge, outFormat).start();
        }
    }

    private static String parseName(String url) throws Exception {
        String html = HttpClientUtil.httpGet(url);
        String data = RegexUtil.find(html, "<div.*?当前位置.*?/div>", 0);
        if (data == null) {
            System.err.println("find name fail");
            return null;
        }
        data = RegexUtil.find(data, "<a href='/vod/.*?>(.*?)</div>", 1);
        if (data == null) {
            System.err.println("find name fail");
            return null;
        }
        data = data.replaceAll("<.*?>", "");
        return data;
    }

    private static String parseIndexM3U8(String url) throws Exception {
        // step1
        String host = UrlUtil.getProtocolHost(url);
        String[] splits = url.substring(url.lastIndexOf("/") + 1, url.length() - 5).split("-");
        String videoId = splits[0]; // 视频id
        String sourceId = splits[1]; // 播放源id
        String episodeId = splits[2]; // 第几集

        // step2
        String html = HttpClientUtil.httpGet(url);
        String url1 = host + RegexUtil.find(html, "/upload/playdata/.*/" + videoId + "/" + videoId + ".js", 0);

        // step3
        String data = HttpClientUtil.httpGet(url1);
        data = JsUtil.eval(RegexUtil.find(data, "unescape\\('.*?'\\)", 0)).toString();
        String[] sources = data.split("\\$\\$\\$");
        String url2 = null;
        for (String source : sources) {
            if (source.contains("kubozy")) {
                String[] episodes = source.split("#");
                String episode = episodes[Integer.parseInt(episodeId) - 1];
                String[] items = episode.split("\\$");
                url2 = items[1];
                if (StringUtils.isNotEmpty(url2)) {
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(url2)) {
            System.err.println("parse url fail");
            return null;
        }

        // step4
        host = UrlUtil.getProtocolHost(url2);
        html = HttpClientUtil.httpGet(url2);
        return host + RegexUtil.find(html, "\"(.*\\.m3u8.*)\"", 1);
    }

    private static File getDownloadRecord(String rootPath, String seasonName) throws IOException {
        File file = new File(rootPath + "/" + seasonName + ".txt");
        System.out.println(rootPath + "/" + seasonName + ".txt");
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                System.err.println("create download record fail");
                return null;
            }
        }
        return file;
    }
}
