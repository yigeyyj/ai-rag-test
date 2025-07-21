package com.hanlinyang.test;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class JGitTest {

    @Test
    public void test() throws Exception {
        String repoURL = "https://gitee.com/cold-welding/PythonProject2.git";
        String userName = "211153697@qq.com";
        String passWord = "215af8c3edcdb1e63ca2a3ce3d54743e";

        String localPath = "./cloned-repo";
        log.info("克隆路径：" + new File(localPath).getAbsolutePath());

        FileUtils.deleteDirectory(new File(localPath));

        Git git = Git.cloneRepository()
                .setURI(repoURL)  // 设置远程仓库地址
                .setDirectory(new File(localPath)) // 设置克隆保存的本地路径
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord)) // 设置认证信息
                .call();  // 执行 clone 操作
        git.close();
    }
    @Test
    public void test_commit_and_push() throws Exception {
        String repoPath = "./cloned-repo/.git"; // 指定本地Git仓库的路径（通常是.git目录）
        String remoteURL = "https://gitee.com/cold-welding/PythonProject2.git";
        String username = "211153697@qq.com";
        String password = "GAOkexin123.";
        String commitMessage = "提交本地修改到远程仓库";

        // 打开Git仓库
        try (Git git = Git.open(new File(repoPath))) {
            // 添加所有修改过的文件到暂存区
            git.add().addFilepattern(".").call();

            // 提交到本地仓库
            git.commit().setMessage(commitMessage).call();

            // 推送到远程仓库
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .setRemote(remoteURL)
                    .setRefSpecs(new RefSpec("HEAD:refs/heads/main")) // 假设你要推送到main分支，根据实际情况调整
                    .call();

            log.info("成功提交并推送到远程仓库");
        }
    }

}
