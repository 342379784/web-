package com.antu.testrpc.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/**
 * 本地测试环境：jdk1.8，tomcat8，i5cpu，8g内存，就一普通的3K左右办公电脑
 * bbb方法是用io流来操作的，传入182M文件后，测试结果如下：
 * 从开始读到写入完成耗时119263
 * 组装数据写入文件共耗时1451
 * ccc方法是用nio中的文件内存映射MapedByteBuffer来操作的，182M文件测试结果如下：
 * 从开始读到写入完成耗时38351
 * 组装数据写入文件共耗时251
 */
@RestController
public class TestController {
    //存放文件流的数组，因为是存放在内存中，所以不建议文件特别大（相对于内存大小）
    InputStream filesStream[] = new InputStream[10];
    Long start = null;
    String realPath = "E:\\uploadFile";
    @RequestMapping(value ="/bbb", produces = "text/plain;charset=UTF-8")
    String bbb(@RequestParam(value = "file", required = false) MultipartFile file,HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        if(start==null)start=System.currentTimeMillis();
        if(request.getParameter("isSuccess") != null){//走到这里说明所有分片已上传完毕，开始组装
            Long writeStart = System.currentTimeMillis();
            System.out.println("开始往文件里写");
            for(int i=0;i<filesStream.length;i++){
                if(filesStream[i] == null) break;
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
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(inputStream != null) inputStream.close();
                    if(outputStream != null) outputStream.close();
                }
            }
            System.out.println("往文件里写内容完成");
            filesStream = new InputStream[10];//初始化files
            System.out.println("从开始读到写入完成耗时"+(System.currentTimeMillis()-start));
            System.out.println("组装数据写入文件共耗时"+(System.currentTimeMillis()-writeStart));
            return "文件保存成功";
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
        return "文件传输完毕";
    }


    MappedByteBuffer mbbs[] = new MappedByteBuffer[10];
    @RequestMapping(value ="/ccc", produces = "text/plain;charset=UTF-8")
    String ccc(@RequestParam(value = "file", required = false) MultipartFile file,HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        if(start==null)start=System.currentTimeMillis();
        if(request.getParameter("isSuccess") != null){//走到这里说明所有分片已上传完毕，开始组装
            Long writeStart = System.currentTimeMillis();
            System.out.println("开始往文件里写");
            for(int i=0;i<mbbs.length;i++){
                if(mbbs[i] == null) break;
                String Ogfilename = request.getParameter("fileName");
                File tempFile = new File(realPath, Ogfilename);

                FileChannel out = null;
                try {
                    out = new FileOutputStream(tempFile,true).getChannel();//这里构造方法多了一个参数true,表示在文件末尾追加写入
                    out.write(mbbs[i]);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(out != null) out.close();;
                }
            }
            System.out.println("往文件里写内容完成");
            mbbs = new MappedByteBuffer[10];//初始化mbbs
            System.out.println("从开始读到写入完成耗时"+(System.currentTimeMillis()-start));
            System.out.println("组装数据写入文件共耗时"+(System.currentTimeMillis()-writeStart));
            return "文件保存成功";
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
            FileInputStream fis = (FileInputStream )file.getInputStream();
            FileChannel fc = fis.getChannel();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());//把文件影射为内存映像文件,指出访问方式为只读
            if(chunk<mbbs.length){
                mbbs[chunk] = mbb;
            }else{//说明超出数组容量的，进行扩容
                MappedByteBuffer tempFiles[] = new MappedByteBuffer[mbbs.length*2];
                System.arraycopy(mbbs,0,tempFiles,0,mbbs.length);
                mbbs = tempFiles.clone();
                mbbs[chunk] = mbb;
            }

        }
        return "文件传输完毕";
    }
}
