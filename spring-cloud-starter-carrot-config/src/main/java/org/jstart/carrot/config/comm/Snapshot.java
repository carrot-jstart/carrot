package org.jstart.carrot.config.comm;

/**
 * 配置内容快照。
 *
 * @param content      配置内容
 * @param md5          配置内容 md5
 * @param lastModified 最后更新时间戳
 * @param contentType  内容类型
 */
public record Snapshot(String content, String md5, long lastModified, String contentType) {
    public Snapshot {
        content = content == null ? "" : content;
        md5 = md5 == null ? "" : md5;
        contentType = contentType == null ? "" : contentType;
    }
}
