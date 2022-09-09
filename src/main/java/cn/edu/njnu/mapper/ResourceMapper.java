package cn.edu.njnu.mapper;

import cn.edu.njnu.pojo.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Mapper
@Repository
public interface ResourceMapper {
    //通过ID获取资源信息
    Resource queryResourceByID(int id);
    ArrayList<Resource> queryResourceByContent(String content, int sort, int type);
    Set<Integer> queryResourceIdByContent(String content);
    ArrayList<Resource> queryResourceByIDList(Set<Integer> IDList, int sort, int type);
    List<Map> queryType();
    //根据ID查相关资源
    String queryRelated(int resource_id);
    List<Map> queryGoalAndKey(String entity);
    //更新资源浏览次数
    boolean updateBrowse(int browse, int resourceID);

    //根据条件查询资源
    ArrayList<Resource> queryHot();
    ArrayList<Resource> queryHot2();
    //根据条件查询资源
    ArrayList<Resource> queryTime();

    //更新下载次数和获取下载次数排行
    int updateDownload(int resourceID);
    ArrayList<Resource> queryDownload(@Param("subject")int subject);

    ArrayList<Resource> queryMoreHot(@Param("subject")int subject);
    //根据条件查询资源
    ArrayList<Resource> queryMoreTime(@Param("subject")int subject);

    Map queryDocument(int id);
    Map queryBvideo(int id);
    Map queryVideo(int id);

    String queryUrl(int resourceID);
    String queryVideoUrl(int resourceID);

    Double resourceRate(int resource);

    List<Resource> queryByGrade(String grade, int sort, int type);
    List<Resource> queryByGradeSmall(int subject, int sort, int type, int page, int pages);
    List<Resource> queryByGradeMiddle(int subject, int sort, int type, int page, int pages);
    List<Resource> queryByGradeHigh(int subject, int sort, int type, int page, int pages);
    int countSmall(@Param("subject")int subject, @Param("type")int type);
    int countMiddle(@Param("subject")int subject, @Param("type")int type);
    int countHigh(@Param("subject")int subject, @Param("type")int type);

    List<Resource> queryByPeriodSubject(int period, int subject, int sort, int type, int page, int pages);
    int countPeriodSubject(int period, int subject);
}
