package com.mengxiang.base.datatask.upload.provider;

//import com.aikucun.common2.base.Result;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mengxiang.base.datatask.model.UploadResult;
import com.mengxiang.base.datatask.upload.provider.model.UploadFileRequestDTO;
import com.mengxiang.base.datatask.util.HttpClient;
import com.mengxiang.base.datatask.util.HttpResponse;
import com.mengxiang.base.datatask.upload.exception.FileUploadException;
import com.mengxiang.base.datatask.upload.provider.model.FileInfo;
import com.mengxiang.base.datatask.upload.provider.model.Result;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class OBSDataUploadProvider implements DataUploadProvider {

    Logger logger = LoggerFactory.getLogger(OBSDataUploadProvider.class);

    private String url;
    private String appid;
    private String secret;

    public OBSDataUploadProvider(String url, String appid, String secret) {
        this.url = url;
        this.appid = appid;
        this.secret = secret;
    }

    @Override
    public UploadResult upload(String uploadFilepath, String parentDirectory, String newFileName, int connectTimeout, int socketTimeout) {
        UploadResult uploadResult = new UploadResult();
        HttpResponse<Result<FileInfo>> rst = null;
        try {
            //logger.info("obs file upload begin:[uploadFilepath={}-parentDirectory={}-newFileName={}]", uploadFilepath, parentDirectory, newFileName);
            //String uploadFilepath = baseFilePath + originalFilename;
            File sourceFile = new File(uploadFilepath);

            //ByteArrayMultipartFile file = new ByteArrayMultipartFile(originalFilename, Files.readAllBytes(sourceFile.toPath()));
            byte[] fileBytes = Files.readAllBytes(sourceFile.toPath());
            UploadFileRequestDTO uploadFileRequestDTO = new UploadFileRequestDTO(
                    sourceFile, appid, parentDirectory, newFileName, Signature.sign(fileBytes, appid, secret), connectTimeout, socketTimeout
            );
            rst = uploadFile(uploadFileRequestDTO);
            if(200 == rst.getCode()) {
                Result<FileInfo> result = rst.getResult();
                //logger.info("obs file upload complete:{}", JSON.toJSONString(result));
                if (result.getSuccess()) {
                    uploadResult.setSuccess(true);
                    uploadResult.setOriginalIploadResult(rst);
                    uploadResult.setFileUrl(result.getData().getFileUrl());
                    return uploadResult;
                    //return result.getData().getFileUrl();
                } else {
                    //logger.warn("文件上传失败：result Code:{}-Message:-{}", result.getCode(), result.getMessage());
                    //throw new FileUploadException("文件上传失败 result Code:" + result.getCode() + " Message:" + result.getMessage());
                }
            }
            logger.warn("文件上传失败：uploadFilepath={}-parentDirectory={}-newFileName={}  http Code:{}-Message:{} rst:{}"
                    , uploadFilepath, parentDirectory, newFileName
                    , rst.getCode(), rst.getMessage()
                    , JSON.toJSONString(rst)
            );
            //throw new FileUploadException("文件上传失败 http Code:" + rst.getCode() + " Message:" + rst.getMessage());
            uploadResult.setOriginalIploadResult(rst);
            uploadResult.setErr(new FileUploadException("文件上传失败 http Code:" + rst.getCode() + " Message:" + rst.getMessage()));
        } catch (Exception e) {
            logger.error("上传OBS失败：uploadFilepath={}-parentDirectory={}-newFileName={}  http Code:{}-Message:{} rst:{}"
                    , uploadFilepath, parentDirectory, newFileName
                    , rst.getCode(), rst.getMessage()
                    , JSON.toJSONString(rst)
                    , e
            );
            //throw new FileUploadException("上传OBS失败", e);
            uploadResult.setOriginalIploadResult(rst);
            uploadResult.setErr(new FileUploadException("上传OBS失败", e));
        }
        return uploadResult;
    }

    public UploadResult upload(String baseFilePath, String originalFilename, String parentDirectory, String newFileName) {
        UploadResult uploadResult = new UploadResult();
        HttpResponse<Result<FileInfo>> rst = null;
        try {
            //logger.info("obs file upload begin:[baseFilePath={}-originalFilename={}-parentDirectory={}-newFileName={}]", baseFilePath, originalFilename, parentDirectory, newFileName);
            String uploadFilepath = baseFilePath + originalFilename;
            File sourceFile = new File(uploadFilepath);

            //ByteArrayMultipartFile file = new ByteArrayMultipartFile(originalFilename, Files.readAllBytes(sourceFile.toPath()));
            byte[] fileBytes = Files.readAllBytes(sourceFile.toPath());
            UploadFileRequestDTO uploadFileRequestDTO = new UploadFileRequestDTO(
                    sourceFile, appid, parentDirectory, newFileName, Signature.sign(fileBytes, appid, secret)
            );
            rst = uploadFile(uploadFileRequestDTO);
            if(200 == rst.getCode()) {
                Result<FileInfo> result = rst.getResult();
                //logger.info("obs file upload complete:{}", JSON.toJSONString(result));
                if (result.getSuccess()) {
                    uploadResult.setSuccess(true);
                    uploadResult.setOriginalIploadResult(rst);
                    uploadResult.setFileUrl(result.getData().getFileUrl());
                    return uploadResult;
                    //return result.getData().getFileUrl();
                } else {
                    //logger.warn("文件上传失败：result Code:{}-Message:-{}", result.getCode(), result.getMessage());
                    //throw new FileUploadException("文件上传失败 result Code:" + result.getCode() + " Message:" + result.getMessage());
                }
            }
            logger.warn("文件上传失败：baseFilePath={}-originalFilename={}-parentDirectory={}-newFileName={} http Code:{}-Message:{}  rst:{}"
                    , baseFilePath, originalFilename, parentDirectory, newFileName
                    , rst.getCode(), rst.getMessage()
                    , JSON.toJSONString(rst)
            );
            uploadResult.setOriginalIploadResult(rst);
            uploadResult.setErr(new FileUploadException("文件上传失败 http Code:" + rst.getCode() + " Message:" + rst.getMessage()));
            //throw new FileUploadException("文件上传失败 http Code:" + rst.getCode() + " Message:" + rst.getMessage());
        } catch (Exception e) {
            logger.error("上传OBS失败：baseFilePath={}-originalFilename={}-parentDirectory={}-newFileName={} http Code:{}-Message:{}  rst:{}"
                    , baseFilePath, originalFilename, parentDirectory, newFileName
                    , rst.getCode(), rst.getMessage()
                    , JSON.toJSONString(rst)
                    , e
            );
            uploadResult.setOriginalIploadResult(rst);
            uploadResult.setErr(new FileUploadException("上传OBS失败", e));
            //throw new FileUploadException("上传OBS失败", e);
        }
        return uploadResult;
    }

    public HttpResponse<Result<FileInfo>> uploadFile(UploadFileRequestDTO uploadFileRequestDTO) {

        File file = uploadFileRequestDTO.getFile();
        String appId = uploadFileRequestDTO.getAppId();
        String parentDirectory = uploadFileRequestDTO.getParentDirectory();
        String fileName = uploadFileRequestDTO.getFileName();
        String signature = uploadFileRequestDTO.getSignature();
        Map<String, ContentBody> mapParam = new HashMap<>();
        mapParam.put("file", new FileBody(file));
        mapParam.put("appId", new StringBody(appId, ContentType.MULTIPART_FORM_DATA));
        mapParam.put("parentDirectory", new StringBody(null == parentDirectory ? "" : parentDirectory, ContentType.MULTIPART_FORM_DATA));
        mapParam.put("fileName", new StringBody(fileName, ContentType.MULTIPART_FORM_DATA));
        mapParam.put("signature", new StringBody(signature, ContentType.MULTIPART_FORM_DATA));
        ;
        logger.info("uploadFile request:" + JSON.toJSONString(uploadFileRequestDTO));
        HttpResponse<Result<FileInfo>> rst = HttpClient.postMultipart(
                url,
                mapParam,
                null,
                new TypeReference<Result<FileInfo>>() {},
                uploadFileRequestDTO.getConnectTimeout(),//3000
                uploadFileRequestDTO.getSocketTimeout());//300000
        logger.info("uploadFile request:" + JSON.toJSONString(uploadFileRequestDTO) + " response:" + JSON.toJSONString(rst));
        return rst;
    }

    /**

    @RequestMapping(path = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileInfo> upload(@RequestPart(value = "file") MultipartFile file,
                                   @RequestParam(value = "appId") String appId,
                                   @RequestParam(value = "parentDirectory", required = false) String parentDirectory,
                                   @RequestParam(value = "fileName", required = false) String fileName,
                                   @RequestParam(value = "signature", required = false) String signature);
     */

}
