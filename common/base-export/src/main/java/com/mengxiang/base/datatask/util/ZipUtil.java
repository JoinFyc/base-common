package com.mengxiang.base.datatask.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void main(String[] args) {

        List<File> srcFiles = new ArrayList<>();
        srcFiles.add(new File("./dataTask-muitl-sheet-1.xlsx"));
        srcFiles.add(new File("./dataTask-muitl-sheet-2.xlsx"));
        srcFiles.add(new File("./testDataExport.xlsx"));
        File zipFile = new File("./dataTask.zip");

        try {
            ZipUtil.zipFiles(srcFiles,zipFile,false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 多个文件压缩
     * @param srcFiles   压缩前的文件
     * @param zipFile    压缩后的文件
     */
    public static void zipFiles(List<File> srcFiles, File zipFile, boolean deleteSourceFile) throws Exception {
        // 判断压缩后的文件存在不，不存在则创建
        if (!zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (Exception e) {
                throw e;
            }
        } else {
            zipFile.delete();
        }
        // 创建 FileOutputStream 对象
        FileOutputStream fileOutputStream = null;
        // 创建 ZipOutputStream
        ZipOutputStream zipOutputStream = null;


        try {
            // 实例化 FileOutputStream 对象
            fileOutputStream = new  FileOutputStream(zipFile);
            // 实例化 ZipOutputStream 对象
            zipOutputStream = new  ZipOutputStream(fileOutputStream);
            // 创建 ZipEntry 对象
            ZipEntry zipEntry = null;
            // 遍历源文件数组
            for (int i = 0; i < srcFiles.size();  i++) {
                // 创建 FileInputStream 对象
                FileInputStream fileInputStream = null;
                if(srcFiles.get(i).exists()) {
                    try {
                        // 将源文件数组中的当前文件读入  FileInputStream 流中
                        fileInputStream = new  FileInputStream(srcFiles.get(i));
                        // 实例化 ZipEntry 对象，源文件数组中的当前文件
                        zipEntry = new  ZipEntry(srcFiles.get(i).getName());
                        zipOutputStream.putNextEntry(zipEntry);
                        // 该变量记录每次真正读的字节个数
                        int len;
                        // 定义每次读取的字节数组
                        byte[] buffer = new byte[1024];
                        while ((len =  fileInputStream.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0,  len);
                        }
                    } catch (Exception e) {
                        if(null != fileInputStream) {
                            try {
                                fileInputStream.close();
                            } catch (Exception e2) {}
                        }
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }finally{

            if(null != zipOutputStream) {
                try {
                    zipOutputStream.flush();
                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
                } catch (Exception e2) {
                    throw e2;
                }
            }
            if(null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (Exception e2) {}
            }

            if(deleteSourceFile) {
                //压缩完成删除压缩前的文件
                for (File file : srcFiles) {
                    if(file.exists()){
                        file.delete();
                    }
                }
            }

        }
    }


}
