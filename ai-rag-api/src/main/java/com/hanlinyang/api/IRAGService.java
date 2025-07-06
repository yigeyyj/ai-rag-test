package com.hanlinyang.api;

import com.hanlinyang.api.response.Response;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRAGService {

    Response<List<String>> queryRagTagList();

    Response<String> uploadFile(String ragTag,List<MultipartFile> files);
}
