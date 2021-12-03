package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.FileType;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.MediaConvertUtil;
import cn.keking.web.filter.BaseUrlFilter;
import org.artofsolving.jodconverter.util.ConfigUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.File;
import java.util.UUID;

/**
 * @author : kl
 * @authorboke : kailing.pub
 * @create : 2018-03-25 上午11:58
 * @description:
 **/
@Service
public class MediaFilePreviewImpl implements FilePreview {

    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;

    private static Object LOCK=new Object();
    private static final Logger logger = LoggerFactory.getLogger(MediaFilePreviewImpl.class);



    public MediaFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        // 不是http开头，浏览器不能直接访问，需下载到本地
        if (url != null && !url.toLowerCase().startsWith("http")) {
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileAttribute.getName());
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            } else {
                url=BaseUrlFilter.getBaseUrl() + fileHandlerService.getRelativePath(response.getContent());
                fileAttribute.setUrl(url);
            }
        }

        if(checkNeedConvert(fileAttribute.getSuffix())){
            model.addAttribute("m3u8Speed", 0);
            url=convertUrl(fileAttribute,model);
            model.addAttribute("mediaUrl", url);
            model.addAttribute("fileName",fileAttribute.getName());
            return MEDIA4M3U8_FILE_PREVIEW_PAGE;
        }else{
            //正常media类型
            String[] medias = ConfigConstants.getMedia();
            for(String media:medias){
                if(media.equals(fileAttribute.getSuffix())){
                    model.addAttribute("mediaUrl", url);
                    return MEDIA_FILE_PREVIEW_PAGE;
                }
            }
            return otherFilePreview.notSupportedFile(model, fileAttribute, "暂不支持");
        }

    }

    /**
     * 检查视频文件处理逻辑
     * 返回处理过后的url
     * @return url
     */
    private String convertUrl(FileAttribute fileAttribute, Model model) {
        String url = fileAttribute.getUrl();
        url = getUrl(url);
        if(fileHandlerService.listConvertedMedias().containsKey(url)){
            return fileHandlerService.getConvertedMedias(url);
        }else {
            // 串行改并行
            // 没有转码
            if (MediaConvertUtil.covertMap.get(url) == null){
                synchronized (MediaConvertUtil.covertMap){
                    if (MediaConvertUtil.covertMap.get(url) == null){
                        MediaConvertUtil.covertMap.put(url,0);
                    }else {
                        // 正在转码
                        logger.info("获取已转码文件大小");
                        model.addAttribute("m3u8Speed", MediaConvertUtil.covertMap.get(url));
                        return "0";
                    }
                }
                // 没有转码，开启转码任务
                logger.info("开始转码：{}",url);
                new Thread(new ConvertTask(fileAttribute, url, fileHandlerService))
                        .start();
                return "0";
            }
            logger.info("获取已转码文件大小11");
            model.addAttribute("m3u8Speed", MediaConvertUtil.covertMap.get(url));
            return "0";
        }
    }

    static class ConvertTask implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(ConvertTask.class);
        private final FileAttribute fileAttribute;
        private final String url;
        private final FileHandlerService fileHandlerService;

        public ConvertTask(FileAttribute fileAttribute,
                           String url,
                           FileHandlerService fileHandlerService) {
            this.fileAttribute = fileAttribute;
            this.url = url;
            this.fileHandlerService = fileHandlerService;
        }

        @Override
        public void run() {
            if (!fileHandlerService.listConvertedMedias().containsKey(url)) {
                String convertedUrl = convertToM3U8(fileAttribute,url);
                // 加入缓存
                fileHandlerService.addConvertedMedias(url, convertedUrl);
                MediaConvertUtil.covertMap.remove(url);
                logger.info("视频转码任务结束：{}",url);
            }
        }

    }


    private static String getUrl(String url){
        if(url.contains("group")&&url.contains("M00")){
            //fastdfs 文件
            return url.substring(url.indexOf("group"),url.lastIndexOf("?"));
        }
        return url;
    }

    public static void main(String[] args) {
        String url = "http://192.168.2.201/group1/M00/00/37/wKgCyWGkmWmESO3IAAAAAI6FniE605.avi?token=0b6ac8c3a351d792895eb95e21725085&ts=1638340547&fullfilename=%E9%97%B9+d%E6%89%93%E5%88%86_ddd.avi";

        String url1 = getUrl(url);
        System.err.println(url1);
    }

    /**
     * 检查视频文件转换是否已开启，以及当前文件是否需要转换
     * @return
     */
    private boolean checkNeedConvert(String suffix) {
        //1.检查开关是否开启
        if("false".equals(ConfigConstants.getMediaConvertDisable())){
            return false;
        }
        //2.检查当前文件是否需要转换
        String[] mediaTypesConvert = FileType.MEDIA_TYPES_CONVERT;
        String type = suffix;
        for(String temp : mediaTypesConvert){
            if(type.equals(temp)){
                return true;
            }
        }
        return false;
    }


    /**
     * 将浏览器不兼容视频格式转换成M3U8
     * @param fileAttribute
     * @param url
     * @return
     */
    private static String convertToM3U8(FileAttribute fileAttribute, String url) {

        //说明：这里做临时处理，取上传文件的目录
        UUID uuid = UUID.randomUUID();
        String name = uuid + "." + fileAttribute.getSuffix();
        logger.info("name:{}",name);
        // 下载至media目录
        ReturnResponse<String> stringReturnResponse = DownloadUtils.downLoad(fileAttribute, name, ConfigConstants.getMediaDir());
        String sourceFilePath = stringReturnResponse.getContent();
        logger.info("sourceFilePath:{}",sourceFilePath);
        String convertFileName=(ConfigConstants.getMediaUrl()+uuid+File.separator+name).replace(fileAttribute.getSuffix(),"m3u8");
        logger.info("convertFileName:{}",convertFileName);
        File file=new File(sourceFilePath);
        // 源文件大小
        String tarFileName = null;
        String dirName = null;
        try {
            tarFileName = file.getAbsolutePath().replace("."+fileAttribute.getSuffix(),File.separator+uuid+".m3u8");
            dirName = file.getAbsolutePath().replace("."+fileAttribute.getSuffix(),"");
            logger.info("dirName:{}",dirName);
            logger.info("tarFileName:{}",tarFileName);
            File desFile=new File(tarFileName);
            File dirFile = new File(dirName);
            //判断一下防止穿透缓存
            if(dirFile.exists()){
                return tarFileName;
            }else {
                dirFile.mkdir();
            }
            if(desFile.exists()){
                return tarFileName;
            }
            MediaConvertUtil.processM3U8(sourceFilePath,tarFileName,url);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //删除源文件
            file.delete();
        }


        return convertFileName;
    }

    /**
     * 将浏览器不兼容视频格式转换成MP4
     * @param fileAttribute
     * @return
     */
    private static String convertToMp4(FileAttribute fileAttribute) {

        //说明：这里做临时处理，取上传文件的目录
        String homePath = ConfigUtils.getHomePath();
        String filePath = homePath+File.separator+"file"+File.separator+"demo"+File.separator+fileAttribute.getName();
        String convertFileName=fileAttribute.getUrl().replace(fileAttribute.getSuffix(),"mp4");

        File file=new File(filePath);
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file);
        String fileName = null;
        Frame captured_frame = null;
        FFmpegFrameRecorder recorder = null;
        try {
            fileName = file.getAbsolutePath().replace(fileAttribute.getSuffix(),"mp4");
            File desFile=new File(fileName);
            //判断一下防止穿透缓存
            if(desFile.exists()){
                return fileName;
            }

            frameGrabber.start();
            recorder = new FFmpegFrameRecorder(fileName, frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
            recorder.setFormat("mp4");
            recorder.setFrameRate(frameGrabber.getFrameRate());
            //recorder.setSampleFormat(frameGrabber.getSampleFormat()); //
            recorder.setSampleRate(frameGrabber.getSampleRate());

            recorder.setAudioChannels(frameGrabber.getAudioChannels());
            recorder.setFrameRate(frameGrabber.getFrameRate());
            recorder.start();
            while ((captured_frame = frameGrabber.grabFrame()) != null) {
                try {
                    recorder.setTimestamp(frameGrabber.getTimestamp());
                    recorder.record(captured_frame);
                } catch (Exception e) {
                }
            }
            recorder.stop();
            recorder.release();
            frameGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //是否删除源文件
        //file.delete();
        return convertFileName;
    }
}
