package cn.edu.njnu.mapper;

import cn.edu.njnu.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Mapper
@Repository
public interface CommentMapper {
    ArrayList<Comment> queryComment(int resourceID);
    boolean addComment(Comment comment);
}
