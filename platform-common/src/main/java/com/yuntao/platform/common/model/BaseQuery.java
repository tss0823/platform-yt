package com.yuntao.platform.common.model;






import com.yuntao.platform.common.annotation.ParamFieldComment;
import com.yuntao.platform.common.web.Pagination;

import java.io.Serializable;

/**
 * Created by shengshan.tang on 2015/11/27 at 14:12
 */
public class BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @ParamFieldComment("页大小")
    private int pageSize = 10; // default 10

    @ParamFieldComment("第几页")
    private long pageNum = 1; // 第几页

    private Pagination pagination;

    private Integer limit;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
