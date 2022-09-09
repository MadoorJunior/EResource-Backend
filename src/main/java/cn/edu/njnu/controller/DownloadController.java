package cn.edu.njnu.controller;


import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.service.DownloadService;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/e-resource/api")
public class DownloadController extends BaseController {

    @Autowired
    private DownloadService downloadService;

    @RequestMapping("/v1.0/public/download")
    public Result downloadFile(@RequestParam Map<String, Object> resourceIDMap, final HttpServletResponse response, final HttpServletRequest request){
        return downloadService.downloadFile(resourceIDMap,response,request);
    }

    @RequiresRoles("teacher")
    @RequestMapping("/v1.0/public/downloadnew")
    public void downloadFileNew(@RequestParam Map<String, Object> resourceIDMap, final HttpServletResponse response, final HttpServletRequest request) throws IOException {
        downloadService.getFile(resourceIDMap, response, request);
    }

    @RequestMapping("/v1.0/private/downloadFolder")
    public void downloadFolder(@RequestParam Map<String, Object> folderIDMap, HttpServletResponse response) throws IOException {
        downloadService.downloadFolder(folderIDMap, response);
    }
}
