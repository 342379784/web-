package com.antu.testrpc.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


@RestController
public class TestController {
    InputStream filesStream[] = new InputStream[10];
    @RequestMapping(value ="/bbb", produces = "text/plain;charset=UTF-8")
    String bbb(@RequestParam(value = "file", required = false) MultipartFile file,HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        String realPath = "E:\\uploadFile";
        if(request.getParameter("isSuccess") != null){//走到这里说明所有分片已上传完毕，开始组装
            for(int i=0;i<filesStream.length;i++){
                if(filesStream[i] == null) return "";
                String Ogfilename = request.getParameter("fileName");
                File tempFile = new File(realPath, Ogfilename);

                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    outputStream = new FileOutputStream(tempFile, true);
                    inputStream = filesStream[i];
                    byte buffer[] = new byte[1024];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                }finally {
                    if(inputStream != null) inputStream.close();
                    if(outputStream != null) outputStream.close();
                }
            }
            filesStream = new InputStream[10];//初始化files
            return "";
        }
        if (request.getParameter("chunk") == null) {

            String fileName = file.getOriginalFilename();

            File targetFile = new File(realPath, fileName);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            file.transferTo(targetFile); // 小文件，直接拷贝
            return "";
        } else {
            int chunk = Integer.parseInt(request.getParameter("chunk")); // 当前分片
            int chunks = Integer.parseInt(request.getParameter("chunks")); // 分片总计
            if(chunk<filesStream.length){
                filesStream[chunk] = file.getInputStream();
            }else{//说明超出数组容量的，进行扩容
                InputStream tempFiles[] = new InputStream[filesStream.length*2];
                System.arraycopy(filesStream,0,tempFiles,0,filesStream.length);
                filesStream = tempFiles.clone();
                filesStream[chunk] = file.getInputStream();
            }

        }
        return "Hello World!";
    }
}
