package cn.edu.njnu.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.njnu.mapper.FavoriteMapper;
import cn.edu.njnu.mapper.ResourceMapper;
import cn.edu.njnu.mapper.UserMapper;
import cn.edu.njnu.mapper.XApiMapper;
import cn.edu.njnu.pojo.Resource;
import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.pojo.ResultFactory;
import com.alibaba.fastjson.JSONObject;

import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.shiro.SecurityUtils;
import org.neo4j.driver.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import static org.neo4j.driver.v1.Values.parameters;

@Service
public class DownloadService {
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private XApiMapper xApiMapper;
    @Autowired
    private UserMapper userMapper;
    @Value("${file.root}")
    private String root;

    private static Driver driver;

    @Autowired
    public DownloadService(Driver driver) {
        DownloadService.driver = driver;
    }

    private  final static String rootPath = "http://s4.z100.vip:7716";

    public void getFile(@RequestParam Map<String, Object> resourceIDMap, final HttpServletResponse response, final HttpServletRequest request) throws IOException {
//        long browseDate = System.currentTimeMillis();
//        String username = (String) SecurityUtils.getSubject().getPrincipal();
//        int userId = userMapper.queryUserByName(username).getUserId();
//
//        //读取路径下面的文件
//        int resourceID = Integer.parseInt((String)resourceIDMap.get("resourceID"));
//        System.out.println(resourceID);
//        int res = resourceMapper.updateDownload(resourceID);
//        System.out.println(res);
//        Resource resource = resourceMapper.queryResourceByID(resourceID);
//        int resourceType = resource.getResourceType();
//        int objectType = resourceType==1?2:3;
////        xApiMapper.addDownload(userId,resourceID,objectType,browseDate);
//        String resultWordPath="";
//        String url="";
//        if (resource.getTable()==2){
//            url = resourceMapper.queryUrl(resourceID);
//        }
//        else if (resource.getTable()==3){
//            url = resourceMapper.queryVideoUrl(resourceID);
//        }
//        else{
//        }
        String resultWordPath = "C:\\Users\\Madoor\\Documents\\docs\\智慧学伴优化建议.docx";
        File file = new File(resultWordPath);
        StringBuilder sb = new StringBuilder();
        for (int i = resultWordPath.length()-1; i >= 0; i--){
            if (resultWordPath.charAt(i)=='.') break;
            sb.append(resultWordPath.charAt(i));
        }
        //获取文件后缀名格式
        String ext = sb.reverse().toString();
        //判断格式,设置相应的输出文件格式
        if(ext.equals("mp4")){
            response.setContentType("video/quicktime");
        }else if(ext.equals("pptx")){
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        }else if(ext.equals("txt")){
            response.setContentType("text/plain");
        }else if(ext.equals("docx")){
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        //读取指定路径下面的文件
        InputStream in = new FileInputStream(file);
        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        //创建存放文件内容的数组
        byte[] buff =new byte[1024];
        //所读取的内容使用n来接收
        int n;
        //当没有读取完时,继续读取,循环
        while((n=in.read(buff))!=-1){
            //将字节数组的数据全部写入到输出流中
            outputStream.write(buff,0,n);
        }
        //强制将缓存区的数据进行输出
        outputStream.flush();
        //关流
        outputStream.close();
        in.close();
    }


    public Result downloadFile(@RequestParam Map<String, Object> resourceIDMap, final HttpServletResponse response, final HttpServletRequest request){
        int resourceID = Integer.parseInt((String)resourceIDMap.get("resourceID"));
        Resource resource = resourceMapper.queryResourceByID(resourceID);
        long browseDate = System.currentTimeMillis();
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        int userId = userMapper.queryUserByName(username).getUserId();
        int resourceType = resource.getResourceType();
        int objectType = resourceType==1?2:3;

        if (resource.getTable()==2){
            String url = resourceMapper.queryUrl(resourceID);
            String resultWordPath = encode(rootPath + url);
            resourceMapper.updateDownload(resourceID);
            xApiMapper.addDownload(userId,resourceID,objectType,browseDate);
            return ResultFactory.buildSuccessResult("下载成功",resultWordPath);
        }
        else if (resource.getTable()==3){
            String url = resourceMapper.queryVideoUrl(resourceID);
            String resultWordPath = encode(rootPath + url);
            resourceMapper.updateDownload(resourceID);
            xApiMapper.addDownload(userId,resourceID,objectType,browseDate);
            return ResultFactory.buildSuccessResult("下载成功",resultWordPath);
        }
        else{
            return ResultFactory.buildFailResult("下载失败");
        }
    }
    public static String encode(String url) {
        try {
            String resultURL = "";
            //遍历字符串
            for (int i = 0; i < url.length(); i++) {
                char charAt = url.charAt(i);
                //只对汉字处理
                if (isChineseChar(charAt)) {
                    String encode = URLEncoder.encode(charAt + "", "UTF-8");
                    resultURL += encode;
                }
                else if ((int)charAt ==32){
                    resultURL += "%20";
                }
                else {
                    resultURL += charAt;
                }
            }
            return resultURL;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    //判断汉字的方法,只要编码在\u4e00到\u9fa5之间的都是汉字
    public static boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    public void downloadFolder(Map<String, Object> folderIDMap, HttpServletResponse response) throws IOException {
        long start = System.currentTimeMillis();
        String folderID = (String) folderIDMap.get("folderID");
        String path = root + "\\download\\" + folderID + "\\";
        File file = new File(path);
        file.mkdirs();
        String fileName = "知识点+学习目标+学习重难点收藏.docx";
        String filePath = path + fileName;
        //创建word
        createWord(path,fileName);
        //写入数据
        ArrayList<String> data = getStr(folderID);
        writeDataDocx(filePath,data,false,12);
        copyAll(folderID, path);
        String zipPath = root + "\\download\\" + folderID + ".zip";
        FileOutputStream fos1 = new FileOutputStream(zipPath);
        toZip(path, fos1, true);
        File zip = new File(zipPath);
        String zipURl = root + "\\download\\" + folderID + ".zip";
        if (zip.exists()){
            File zipFile = new File(zipURl);
            //读取指定路径下面的文件
            InputStream in = new FileInputStream(zipFile);
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            //创建存放文件内容的数组
            byte[] buff =new byte[1024];
            //所读取的内容使用n来接收
            int n;
            //当没有读取完时,继续读取,循环
            while((n=in.read(buff))!=-1){
                //将字节数组的数据全部写入到输出流中
                outputStream.write(buff,0,n);
            }
            //强制将缓存区的数据进行输出
            outputStream.flush();
            //关流
            outputStream.close();
            in.close();
        }
//        else {
//            return ResultFactory.buildFailResult("下载失败");
//        }
    }
    //获取收藏的文本
    public ArrayList<String> getStr(String folderID){
        Session session = driver.session();//已关
        ArrayList<String> text = new ArrayList<>();
        ArrayList<Map> contentList = favoriteMapper.collectionStr(folderID);
        text.add("知识点");
        for(Map contentMap:contentList){
            text.add((String) contentMap.get("content"));
        }
        ArrayList<Map> keyList = favoriteMapper.key(folderID);
        text.add(" ");
        text.add("学习重难点");
        if (keyList.size()>0){
            for (Map singleKey:keyList){
                if (singleKey!=null){
                    int id = (int) singleKey.get("key");
                    StatementResult node = session.run( "MATCH (n:GoalAndKey) where n.id = {id} " +
                                    "RETURN n.key as key",
                            parameters( "id", id) );
                    while ( node.hasNext() )
                    {
                        Record record = node.next();
                        String keyText = record.get( "key" ).asString();
                        text.add(keyText);
                    }
                }

            }
        }
        ArrayList<Map> goalList = favoriteMapper.goal(folderID);
        text.add(" ");
        text.add("学习目标");
        if (keyList.size()>0){
            for (Map singleGoal:goalList){
                if (singleGoal!=null){
                    int id = (int) singleGoal.get("goal");
                    StatementResult node = session.run( "MATCH (n:GoalAndKey) where n.id = {id} " +
                                    "RETURN n.goal as goal",
                            parameters( "id", id) );
                    while ( node.hasNext() )
                    {
                        Record record = node.next();
                        String goalText = record.get( "goal" ).asString();
                        text.add(goalText);
                    }
                }

            }
        }
        session.close();
        return text;
    }
    public static void createWord(String path, String fileName) {
        //判断目录是否存在
        File file = new File(path);
        //exists()测试此抽象路径名表示的文件或目录是否存在。
        //mkdir()创建此抽象路径名指定的目录。
        //mkdirs()创建此抽象路径名指定的目录，包括所有必需但不存在的父目录。
        if (!file.exists()) {
            file.mkdirs();
        }
        //因为HWPFDocument并没有提供公共的构造方法 所以没有办法构造word
        //这里使用word2007及以上的XWPFDocument来进行构造word
        @SuppressWarnings("resource")
        XWPFDocument document = new XWPFDocument();
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(new File(file, fileName));
            document.write(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) ;
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //向word中写入数据
    /**
     * 有些方法需要传特殊类型的参数的时候，一般可以用★静态的接口.参数★来传参
     *
     * @param path
     * @param data
     */
    public static void writeDataDocx(String path, ArrayList<String> data, boolean jiacu, int size) {
        InputStream istream = null;
        OutputStream ostream = null;
        try {
            istream = new FileInputStream(path);
            ostream = new FileOutputStream(path);
            @SuppressWarnings("resource")
            XWPFDocument document = new XWPFDocument();
            Iterator textList = data.iterator();
            while(textList.hasNext()){
                String text = (String) textList.next();
                //添加一个段落
                XWPFParagraph p = document.createParagraph();
                XWPFRun r = p.createRun();//p1.createRun()将一个新运行追加到这一段
                r.setText(text);
            }
            /**
             * r1.setDocumentbackground(doc, "FDE9D9");//设置页面背景色
             r1.testSetUnderLineStyle(doc);//设置下划线样式以及突出显示文本
             r1.addNewPage(doc, BreakType.PAGE);
             r1.testSetShdStyle(doc);//设置文字底纹
             */
            document.write(ostream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ostream != null) {
                try {
                    ostream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void copyAll(String folderID, String path){
        ArrayList<Resource> collection = favoriteMapper.collection(folderID);
        for (Resource resource:collection){
            int id = resource.getId();
            String url = resourceMapper.queryUrl(id);
            resourceMapper.updateDownload(id);
            copyFile(root + url, path);
        }
    }
    public void copyFile(String oldPath, String newPath) {
        int l = oldPath.split("/").length;
        newPath = newPath + oldPath.split("/")[l-1];
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 压缩成ZIP 方法1
     * @param srcDir 压缩文件夹路径
     * @param out    压缩文件输出流
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String srcDir, OutputStream out, boolean KeepDirStructure)
        throws RuntimeException{
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile,zos,sourceFile.getName(),KeepDirStructure);
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param name       压缩后的名称
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure) throws Exception{
        int  BUFFER_SIZE = 2 * 1024;
        byte[] buf = new byte[BUFFER_SIZE];
        if(sourceFile.isFile()){
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(KeepDirStructure){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            }else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(),KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(),KeepDirStructure);
                    }
                }
            }
        }
    }
}
