package com.wchgogo;

import com.wchgogo.utils.HttpClientUtil;
import com.wchgogo.utils.MediaUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Downloader {
    private final String url;
    private final String videoName;
    private final String targetFolder;
    private final int threadNum;
    private final boolean purgeTempFile;
    private final String tempFolder;
    private final String mergeFile;
    private final String outFormat;


    public Downloader(String url, String videoName, String targetFolder,
                      int threadNum, boolean purgeTempFile, String outFormat) {
        this.url = url;
        this.videoName = videoName.replaceAll("/", "-");
        this.targetFolder = targetFolder + (targetFolder.endsWith("/") ? "" : "/");
        this.threadNum = threadNum;
        this.purgeTempFile = purgeTempFile;
        this.tempFolder = this.targetFolder + "tmp/" + this.videoName;
        this.mergeFile = this.targetFolder + this.videoName + ".ts";
        this.outFormat = outFormat;
    }

    public void start() throws Exception {
        long startTime = System.currentTimeMillis();
        download();
        merge();
        purge();
        convertFormat();
        long elapseTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("恭喜下载完成！耗时" + elapseTime + "秒");
    }

    private void convertFormat() {
        if (StringUtils.isNotEmpty(outFormat)) {
            try {
                MediaUtil.convertFormat(mergeFile, outFormat, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void purge() {
        if (this.purgeTempFile) {
            try {
                System.out.println("开始清除临时文件");
                FileUtils.deleteDirectory(new File(this.tempFolder));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void merge() throws Exception {
        Queue<String> videoUrls = parseVideoUrl();
        System.out.println("开始合并视频");
        OutputStream os = new FileOutputStream(new File(mergeFile), true);
        for (String videoUrl : videoUrls) {
            String filename = videoUrl.substring(videoUrl.lastIndexOf("/"));
            String filepath = this.tempFolder + "/" + filename;
            File file = new File(filepath);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(filepath);
                IOUtils.copy(fis, os);
                fis.close();
                System.out.println("合并 " + filename);
            } else {
                System.err.println("跳过 " + filename);
            }
        }
        os.flush();
        os.close();
        System.out.println("视频合并完成");
    }

    private void download() throws Exception {
        final Queue<String> videoUrls = parseVideoUrl();
        final CountDownLatch latch = new CountDownLatch(threadNum);
        final int total = videoUrls.size();
        System.out.println("开始下载视频");
        for (int i = 0; i < threadNum; i++) {
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        String videoUrl = videoUrls.poll();
                        if (videoUrl == null) {
                            latch.countDown();
                            break;
                        }
                        String filename = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
                        System.out.println("开始下载 " + filename);
                        boolean success = false;
                        String filepath = Downloader.this.tempFolder + "/" + filename;
                        for (int retry = 0; retry < 3; retry++) {
                            try {
                                CloseableHttpResponse response = HttpClientUtil.client.execute(new HttpGet(videoUrl));
                                InputStream is = response.getEntity().getContent();
                                FileUtils.copyInputStreamToFile(is, new File(filepath));
                                success = true;
                                break;
                            } catch (Exception e) {
                                System.err.println(filename + " 第" + retry + "次下载失败");
                            }
                        }
                        if (!success) {
                            try {
                                videoUrls.add(videoUrl);
                                FileUtils.forceDeleteOnExit(new File(filepath));
                            } catch (Exception e1) {

                            }
                        }
                        System.out.println(filename + " 下载成功，剩余 " + videoUrls.size() + "/" + total);
                    }
                }
            }).start();
        }
        latch.await();
        System.out.println("下载完成！");
    }

    private Queue<String> parseVideoUrl() {
        Queue<String> videoUrls = new LinkedBlockingQueue<String>();
        System.out.println("开始解析视频地址");
        try {
            String html = HttpClientUtil.httpGet(url);
            String tmp = html.replaceAll("#.*\n", "").trim();
            String url2;
            if (tmp.contains(".m3u8")) {
                if (tmp.startsWith("/")) {
                    url2 = new URL(url).getProtocol() + "://" + new URL(url).getHost() + tmp;
                } else {
                    url2 = url.substring(0, url.lastIndexOf("/") + 1) + tmp;
                }
                System.out.println("从 " + url + " 解析出 " + url2);
            } else {
                url2 = url;
            }

            html = HttpClientUtil.httpGet(url2);
            int i = 0;
            for (String filename : html.replaceAll("#.*\n", "").split("\n")) {
                String videoUrl;
                filename = filename.trim();
                if (filename.startsWith("/") || filename.startsWith("http:")) {
                    videoUrl = new URL(url2).getProtocol() + "://" + new URL(url2).getHost() + filename;
                } else {
                    videoUrl = url2.substring(0, url2.lastIndexOf("/") + 1) + filename;

                }
                videoUrls.add(videoUrl);
            }
            System.out.println("从 " + url2 + " 解析出 " + videoUrls.size() + " 个视频地址");
        } catch (IOException e) {
            System.err.println("解析视频地址失败");
            e.printStackTrace();
        }
        return videoUrls;
    }


}
