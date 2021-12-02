package cn.keking.model;

/**
 * @program: filepreview
 * @description: m3u8转换进度
 * @author: 余成
 * @create: 2021-12-02 13:39
 **/
public class M3U8Speed {

    private String sourceDir;

    private Long sourceSize = 0L;

    private Long targetSize = 0L;

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public Long getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(Long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public Long getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(Long targetSize) {
        this.targetSize = targetSize;
    }
}
