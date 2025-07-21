package com.hanlinyang.trigger.controller;

import com.hanlinyang.api.IRAGService;
import com.hanlinyang.api.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin("*")
@RequestMapping("/api/v1/rag/")
public class RAGController implements IRAGService {
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private RedissonClient redissonClient;

    @Override
    @GetMapping("/query_rag_tag_list")
    public Response<List<String>> queryRagTagList() {
        RList<String> elements = redissonClient.getList("ragTag");
        return Response.<List<String>>builder()
                .code("0000")
                .info("调用成功")
                .data(new ArrayList<>(elements))
                .build();
    }

    @Override
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<String> uploadFile(@RequestParam String ragTag, @RequestParam("file") List<MultipartFile> files) {
        log.info("上传知识库开始 {}", ragTag);
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // 添加知识库标签
            documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));

            pgVectorStore.accept(documentSplitterList);

            // 添加知识库记录
            RList<String> elements = redissonClient.getList("ragTag");
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }

        log.info("上传知识库完成 {}", ragTag);
        return Response.<String>builder().code("0000").info("调用成功").build();
    }

    @Override
    public Response<String> analyzeGitRepository(String repoUrl, String userName, String token)  {
        String localPath = "./git-cloned-repo";
        String projectName = extractProjectName(repoUrl);
        log.info("克隆路径：{}",new File(localPath).getAbsolutePath());

        FileUtils.deleteQuietly(new File(localPath));
        try {
            Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, token))
                    .call();// 执行 clone 操作
            Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    log.info("{} 遍历解析路径，上传知识库:{}", projectName, file.getFileName());
                    try {
                        TikaDocumentReader reader = new TikaDocumentReader(new PathResource(file));
                        List<Document> documents = reader.get();
                        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                        documents.forEach(doc -> doc.getMetadata().put("knowledge", projectName));

                        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", projectName));

                        pgVectorStore.accept(documentSplitterList);
                    } catch (Exception e) {
                        log.error("遍历解析路径，上传知识库失败:{}", file.getFileName());
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    log.info("Failed to access file: {} - {}", file.toString(), exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });


            FileUtils.deleteDirectory(new File(localPath));

            // 添加知识库记录
            RList<String> elements = redissonClient.getList("ragTag");
            if (!elements.contains(projectName)) {
                elements.add(projectName);
            }

            git.close();
        } catch (Exception e) {
            log.info("拉取失败");
            throw new RuntimeException(e);
        }
        log.info("遍历解析路径，上传完成:{}", repoUrl);
        return Response.<String>builder().code("0000").info("调用成功").build();
    }

    /**
     * 获取项目名称
     * @param repoUrl
     * @return
     */
    private String extractProjectName(String repoUrl) {
        String[] parts = repoUrl.split("/");
        String projectNameWithGit = parts[parts.length - 1];
        return projectNameWithGit.replace(".git", "");
    }

}
