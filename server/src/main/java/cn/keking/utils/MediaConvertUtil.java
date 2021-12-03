package cn.keking.utils;


import cn.keking.config.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  mp4转换m3u8工具类
 */
@Component
public class MediaConvertUtil {

    Logger log = LoggerFactory.getLogger(MediaConvertUtil.class);

    public static final Map<String, Integer> covertMap;

    static {
        covertMap =new ConcurrentHashMap<>();
    }

    // ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
    /**
     * ffmpeg程序转换m3u8
     * @return
     */
    public static boolean  processM3U8(String sourceFilePath,  String tarFilePath,String key) {
        //这里就写入执行语句就可以了
        List commend = new java.util.ArrayList();
        commend.add(ConfigConstants.getFfmpegPath());
        commend.add("-i");
        commend.add(sourceFilePath);
        commend.add("-c:v");
        commend.add("libx264");
        commend.add("-hls_time");
        commend.add("20");
        commend.add("-hls_list_size");
        commend.add("0");
        commend.add("-c:a");
        commend.add("aac");
        commend.add("-strict");
        commend.add("-2");
        commend.add("-f");
        commend.add("hls");
        commend.add(tarFilePath);
        try {
            //java
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commend);
            Process p = builder.start();
//            int i = doWaitFor(p,userAccount);
//            log.info("------>"+i);
            doWaitPro(p,key);

            p.destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 监听ffmpeg运行过程
     * @param p
     * @param userAccount
     * @return
     */
    public int doWaitFor(Process p, String userAccount) {
        InputStream in = null;
        InputStream err = null;
        // returned to caller when p is finished
        int exitValue = -1;
        try {
            log.info("comeing");
            in = p.getInputStream();
            err = p.getErrorStream();
            // Set to true when p is finished
            boolean finished = false;

            while (!finished) {
                try {
                    while (in.available() > 0) {
                        Character c = new Character((char) in.read());
                        System.out.print(c);
                    }
                    while (err.available() > 0) {
                        Character c = new Character((char) err.read());
                        System.err.print(c);
                    }

                    exitValue = p.exitValue();
                    finished = true;
                    log.info("----"+userAccount);
                } catch (IllegalThreadStateException e) {
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            log.error("doWaitFor();: unexpected exception - "
                    + e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

            } catch (IOException e) {
                log.error("",e);
            }
            if (err != null) {
                try {
                    err.close();
                } catch (IOException e) {
                    log.error("",e);
                }
            }
        }
        return exitValue;
    }

    //等待线程处理完成
    public static void doWaitPro(Process p, String key){
        try {
            String errorMsg = readInputStream(p.getErrorStream(), key);
            String outputMsg = readInputStream(p.getInputStream(), key);
            int c = p.waitFor();
            if (c != 0) {// 如果处理进程在等待
                System.out.println("处理失败：" + errorMsg);
            } else {
                System.out.println("" + outputMsg);
            }
        } catch (IOException e) {
            // tanghui Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // tanghui Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     *
     * @Title: readInputStream
     * @Description: 完成进度百分比
     * @param
     * @return String
     * @throws
     */
    private static String readInputStream(InputStream is,String key) throws IOException {
        int complete = 0;
        // 将进程的输出流封装成缓冲读者对象
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer lines = new StringBuffer();// 构造一个可变字符串
        long totalTime = 0;
        int before = -1;

        // 对缓冲读者对象进行每行循环
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            lines.append(line);// 将每行信息字符串添加到可变字符串中
            int positionDuration = line.indexOf("Duration:");// 在当前行中找到第一个"Duration:"的位置
            int positionTime = line.indexOf("time=");
            if (positionDuration > 0) {// 如果当前行中有"Duration:"
                String dur = line.replace("Duration:", "");// 将当前行中"Duration:"替换为""
                dur = dur.trim().substring(0, 8);// 将替换后的字符串去掉首尾空格后截取前8个字符
                int h = Integer.parseInt(dur.substring(0, 2));// 封装成小时
                int m = Integer.parseInt(dur.substring(3, 5));// 封装成分钟
                int s = Integer.parseInt(dur.substring(6, 8));// 封装成秒
                totalTime = h * 3600 + m * 60 + s;// 得到总共的时间秒数
            }
            if (positionTime > 0) {// 如果所用时间字符串存在
                // 截取包含time=的当前所用时间字符串
                String time = line.substring(positionTime, line
                        .indexOf("bitrate") - 1);
                time = time.substring(time.indexOf("=") + 1, time.indexOf("."));// 截取当前所用时间字符串
                int h = Integer.parseInt(time.substring(0, 2));// 封装成小时
                int m = Integer.parseInt(time.substring(3, 5));// 封装成分钟
                int s = Integer.parseInt(time.substring(6, 8));// 封装成秒
                long hasTime = h * 3600 + m * 60 + s;// 得到总共的时间秒数
                float t = (float) hasTime / (float) totalTime;// 计算所用时间与总共需要时间的比例
                complete = (int) Math.ceil(t * 100);// 计算完成进度百分比
            }
            if(complete>before&&complete<100){
                before = complete;
                covertMap.put(key,complete);
            }
        }
        br.close();// 关闭进程的输出流
        return lines.toString();
    }

    public static void main(String[] args) {
        MediaConvertUtil.processM3U8("F:\\techData\\闹 d打分_ddd.avi","F:\\techData\\test1\\a.m3u8","/group");



    }


}

