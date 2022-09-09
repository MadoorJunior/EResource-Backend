package cn.edu.njnu.mapper;

import cn.edu.njnu.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface XApiMapper {
    void addQuery(int userId, long browseDate, String keyword);
    void addClickEntity(int userId, long browseDate, String keyword);
    void addBrowse(int userId, int resourceID, int objectType, long browseDate, int from);
    void addFavorite(int userId, int resourceID, String folderName,int objectType, long browseDate);
    void addDownload(int userId, int resourceID, int objectType, long browseDate);
    void addComment(Comment comment);
}
