package com.cityrepair.common;

public class PageQuery {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String status;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page == null || page < 1 ? 1 : page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            this.pageSize = 10;
        } else {
            this.pageSize = Math.min(pageSize, 100);
        }
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
