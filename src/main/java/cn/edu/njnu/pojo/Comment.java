package cn.edu.njnu.pojo;

public class Comment {

    int userID;
    int resourceID;
    String content;
    String date;
    double rate;
    String avatar;
    String username;
    long browseDate;
    int resourceType;

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public long getBrowseDate() {
        return browseDate;
    }

    public void setBrowseDate(long browseDate) {
        this.browseDate = browseDate;
    }

    public Comment() {
    }

    @Override
    public String toString() {
        return "Comment{" +
                "userID=" + userID +
                ", resourceID=" + resourceID +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", rate=" + rate +
                ", avatar='" + avatar + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    public Comment(int userID, int resourceID, String content, String date, double rate, String avatar, String username) {
        this.userID = userID;
        this.resourceID = resourceID;
        this.content = content;
        this.date = date;
        this.rate = rate;
        this.avatar = avatar;
        this.username = username;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
