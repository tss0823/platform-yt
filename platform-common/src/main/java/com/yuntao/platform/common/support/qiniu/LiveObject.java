package com.yuntao.platform.common.support.qiniu;

import java.io.Serializable;

/**
 * Created by tangshengshan on 17-1-7.
 */
public class LiveObject implements Serializable {

    private String publishUrl;

    private String playUrl;

    private String hlsPlayUrl;

    private String hdlPlayUrl;

    private String snapshotPlayUrl;


    public String getPublishUrl() {
        return publishUrl;
    }

    public void setPublishUrl(String publishUrl) {
        this.publishUrl = publishUrl;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getHlsPlayUrl() {
        return hlsPlayUrl;
    }

    public void setHlsPlayUrl(String hlsPlayUrl) {
        this.hlsPlayUrl = hlsPlayUrl;
    }

    public String getHdlPlayUrl() {
        return hdlPlayUrl;
    }

    public void setHdlPlayUrl(String hdlPlayUrl) {
        this.hdlPlayUrl = hdlPlayUrl;
    }

    public String getSnapshotPlayUrl() {
        return snapshotPlayUrl;
    }

    public void setSnapshotPlayUrl(String snapshotPlayUrl) {
        this.snapshotPlayUrl = snapshotPlayUrl;
    }
}
