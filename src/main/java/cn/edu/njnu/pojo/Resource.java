package cn.edu.njnu.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Resource implements Serializable {
    int id;
    int collectionId;
    String resourceName;
    String remark;

    String url;
    String viewUrl;
    String cover;

    String aid;
    String bvid;
    String cid;
    int page;

    String keywords;
    ArrayList<String> entityList;
    String updateTime;
    int download;
    int collection;
    int browse;
    int resourceType;
    String period;
    String grade;
    int subject;

    int table;
    Integer tableResourceID;

    double rate;
    String fileType;
    String content;

    public Resource() {
    }

    public Resource(int id, String resourceName, String remark, String url, String viewUrl, String cover, String aid, String bvid, String cid, int page, String keywords, ArrayList<String> entityList, String updateTime, int download, int collection, int browse, int resourceType, String period, String grade, int subject, int table, Integer tableResourceID, double rate, String fileType, String content) {
        this.id = id;
        this.resourceName = resourceName;
        this.remark = remark;
        this.url = url;
        this.viewUrl = viewUrl;
        this.cover = cover;
        this.aid = aid;
        this.bvid = bvid;
        this.cid = cid;
        this.page = page;
        this.keywords = keywords;
        this.entityList = entityList;
        this.updateTime = updateTime;
        this.download = download;
        this.collection = collection;
        this.browse = browse;
        this.resourceType = resourceType;
        this.period = period;
        this.grade = grade;
        this.subject = subject;
        this.table = table;
        this.tableResourceID = tableResourceID;
        this.rate = rate;
        this.fileType = fileType;
        this.content = content;
    }

    //重写equals和hashCode，只要两个resource对象id属性相等则表示为两个同样的资源
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return id == resource.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getBvid() {
        return bvid;
    }

    public void setBvid(String bvid) {
        this.bvid = bvid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public ArrayList<String> getEntityList() {
        return entityList;
    }

    public void setEntityList(ArrayList<String> entityList) {
        this.entityList = entityList;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getDownload() {
        return download;
    }

    public void setDownload(int download) {
        this.download = download;
    }

    public int getCollection() {
        return collection;
    }

    public void setCollection(int collection) {
        this.collection = collection;
    }

    public int getBrowse() {
        return browse;
    }

    public void setBrowse(int browse) {
        this.browse = browse;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
    }

    public int getTable() {
        return table;
    }

    public void setTable(int table) {
        this.table = table;
    }

    public Integer getTableResourceID() {
        return tableResourceID;
    }

    public void setTableResourceID(Integer tableResourceID) {
        this.tableResourceID = tableResourceID;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
