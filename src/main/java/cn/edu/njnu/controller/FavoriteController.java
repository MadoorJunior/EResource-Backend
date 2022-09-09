package cn.edu.njnu.controller;

import cn.edu.njnu.pojo.Result;
import cn.edu.njnu.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/e-resource/api")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/v1.0/private/folder")
    public Result favorite(@RequestParam Map<String, Object> infoMap){
        return favoriteService.favorite(infoMap);
    }

    @GetMapping("/v1.0/private/folderResource/{folderID}")
    public Result folderResource(@PathVariable(name = "folderID") String folderID){
        return favoriteService.folderResource(folderID);
    }

    @PostMapping("/v1.0/private/createFolder")
    public Result createFolder(@RequestBody Map<String, Object> infoMap){
        return favoriteService.createFolder(infoMap);
    }

    @PostMapping("/v1.0/private/putInFolder")
    public Result putInFolder(@RequestBody Map<String, Object> IDMap){
        return favoriteService.putInFolder(IDMap);
    }

    @DeleteMapping("/v1.0/private/folder/{folderID}")
    public Result deleteFolder(@PathVariable(name = "folderID") String folderID){
        return favoriteService.deleteFolder(folderID);
    }

    @PutMapping("/v1.0/private/updateFolder")
    public Result updateFolder(@RequestBody Map<String, Object> infoMap){
        return favoriteService.updateFolder(infoMap);
    }

    @DeleteMapping("/v1.0/private/delSingle")
    public Result delSingle(@RequestBody Map<String, Object> IDMap){
        return favoriteService.delSingle(IDMap);
    }

    @DeleteMapping("/v1.0/private/delMulti")
    public Result delMulti(@RequestBody Map<String, Object> IDMap){
        return favoriteService.delMulti(IDMap);
    }

}
