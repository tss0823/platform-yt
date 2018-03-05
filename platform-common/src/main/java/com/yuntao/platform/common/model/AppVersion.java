/*
 * 
 * 
 * 
 * 
 */

package com.yuntao.platform.common.model;

import com.yuntao.platform.common.annotation.ReturnFieldComment;

import java.io.Serializable;

/**
 * 应用版本
 * @author admin
 *
 * @2017-03-18 16
 */
public class AppVersion implements Serializable {
    
    private static final long serialVersionUID = 1L;
    

    /**  id * */
    private Long id;

    @ReturnFieldComment("是否需要更新(false 不需要，true 需要)")
    private Boolean needUpdate = false;

    @ReturnFieldComment("更新后的版本")
    private String version;

    @ReturnFieldComment("app下载链接地址")
    private String appUrl;
        
    @ReturnFieldComment("是否强制更新(false 可选择更新，true 强制更新)")
    private Boolean forceUpdate;

    @ReturnFieldComment("更新日志")
    private String updateLog;

        
    
    public AppVersion(){
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Boolean getForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(Boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }


    public Boolean getNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(Boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }
}