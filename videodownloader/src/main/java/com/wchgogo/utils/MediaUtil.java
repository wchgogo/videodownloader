package com.wchgogo.utils;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MediaUtil {

    public static boolean convertFormat(String src, String outFormat, boolean deleteSrcOnSuccess) throws Exception {
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            System.err.println("转码文件不存在");
            return false;
        }

        Properties properties = new Properties();
        properties.load(MediaUtil.class.getResourceAsStream("/config/config.properties"));
        String exePath = properties.getProperty("ffmpeg_exe_path");
        if (StringUtils.isEmpty(exePath) || !new File(exePath).exists()) {
            System.err.println("ffmpeg文件不存在");
            return false;
        }
        List<String> commands = new ArrayList<>();
        commands.add(exePath);
        commands.add("-i");
        commands.add(srcFile.getAbsolutePath());
        commands.add("-vcodec");
        commands.add("copy");
        commands.add("-acodec");
        commands.add("copy");
        commands.add(srcFile.getParent() + "/" + srcFile.getName().substring(0, srcFile.getName().lastIndexOf(".")) + "." + outFormat);
        boolean success = executeCommand(commands);
        if (success && deleteSrcOnSuccess) {
            try {
                srcFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    private static boolean executeCommand(List<String> commands) throws IOException {
        Process process = null;
        try {
            // 执行ffmpeg指令
            process = new ProcessBuilder().command(commands).start();
            // 等待ffmpeg命令执行完
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                System.err.println("转码异常");
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
