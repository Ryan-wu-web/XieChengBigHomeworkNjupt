package com.yisu.server.controller;

import cn.hutool.core.util.IdUtil;
import com.yisu.server.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {

    @Value("${server.port}")
    private String port;

    private static final String ip = "192.168.3.10";

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String flag = IdUtil.fastSimpleUUID();
        String rootFilePath = System.getProperty("user.dir") + "/src/main/resources/static/uploads/" + flag + "_" + originalFilename;
        
        File saveFile = new File(rootFilePath);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }
        
        file.transferTo(saveFile);
        
        String url = "http://" + ip + ":" + port + "/uploads/" + flag + "_" + originalFilename;
        return Result.success(url);
    }
}
