package cn.zwq.util;

import com.jcraft.jsch.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author zhangwenqia
 * @create 2022-07-19 17:49
 * @description 类描述
 */
public class SftpClientUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Sftp
     */
    private ChannelSftp sftp = null;
    /**
     * 主机
     */
    private String host = "";
    /**
     * 端口
     */
    private int port = 0;
    /**
     * 用户名
     */
    private String username = "";
    /**
     * 密码
     */
    private String password = "";

    /**
     * 构造函数
     *
     * @param host     主机
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     */
    public SftpClientUtil(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * 连接sftp服务器
     *
     * @throws Exception
     */
    public void connect() throws JSchException {
        JSch jsch = new JSch();
        Session sshSession = jsch.getSession(this.username, this.host, this.port);

        sshSession.setPassword(password);
        Properties sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        sshSession.setConfig(sshConfig);
        sshSession.connect(20000);

        Channel channel = sshSession.openChannel("sftp");
        channel.connect();
        this.sftp = (ChannelSftp) channel;
    }

    /**
     * Disconnect with server
     *
     * @throws Exception
     */
    public void disconnect() {
        if (this.sftp != null && this.sftp.isConnected()) {
            this.sftp.disconnect();
        }
    }

    /**
     * 下载单个文件
     *
     * @param directory     下载目录
     * @param downloadFile  下载的文件
     * @param saveDirectory 存在本地的路径
     * @throws Exception
     */
    public void download(String directory, String downloadFile, String saveDirectory) throws SftpException, IOException, URISyntaxException {
        FileUtils.forceMkdir(new File(saveDirectory));

        String saveFile = String.format("%s%s%s", saveDirectory, File.separator, downloadFile);
        URI uri = new URL(saveFile).toURI();

        this.sftp.cd(directory);
        this.sftp.get(downloadFile, new FileOutputStream(Paths.get(uri).toString()));
    }

    /**
     * 下载目录下全部文件
     *
     * @param directory     下载目录
     * @param saveDirectory 存在本地的路径
     * @throws Exception
     */
    public void downloadByDirectory(String directory, String saveDirectory) throws SftpException, IOException, URISyntaxException {
        String downloadFile = "";
        List<String> downloadFileList = this.listFiles(directory);
        Iterator<String> it = downloadFileList.iterator();

        while (it.hasNext()) {
            downloadFile = it.next();
            if (downloadFile.indexOf(".") != -1) {
                this.download(directory, downloadFile, saveDirectory);
            }
        }
    }

    /**
     * 新建子目录
     *
     * @param dst 远程服务器路径
     * @throws Exception
     */
    public void mkdir(String dst, String subDir) throws SftpException {
        this.sftp.cd(dst);
        try {
            if (!this.sftp.ls(subDir).isEmpty()) {
                return;
            }
        } catch (SftpException se) {
            logger.error("创建目录前检查目录是否存在异常", se);
        }

        this.sftp.mkdir(subDir);
    }

    /**
     * 上传单个文件
     *
     * @param src 文件
     * @param dst 远程服务器路径
     * @throws Exception
     */
    public void upload(InputStream src, String dst) throws SftpException {
        logger.info("开始上传文件：{}", dst);
        this.sftp.put(src, dst);
        logger.info("结束上传文件：{}", dst);
    }

    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录
     * @return list 文件名列表
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<String> listFiles(String directory) throws SftpException {
        List<String> fileNameList = new ArrayList<>();

        List<ChannelSftp.LsEntry> fileList = new ArrayList<>(this.sftp.ls(directory));
        Iterator<ChannelSftp.LsEntry> it = fileList.iterator();

        while (it.hasNext()) {
            String fileName = it.next().getFilename();
            if (!(".".equals(fileName) || "..".equals(fileName))) {
                fileNameList.add(fileName);
            }
        }

        return fileNameList;
    }

    /**
     * 删除目标文件
     *
     * @param dst
     */
    public void delete(String dst) throws SftpException {
        logger.info("开始删除文件：{}", dst);
        this.sftp.rm(dst);
        logger.info("结束删除文件：{}", dst);
    }

    /**
     * 迁移目标文件
     *
     * @return
     */
    public void update(String src, String dst, String fileName) throws SftpException {
        if (!isExistDir(dst)) {
            this.sftp.mkdir(dst);
        }

        String oldPath = Paths.get(src, fileName).toString();
        String newPath = Paths.get(dst, fileName).toString();

        logger.info("开始迁移文件：{} -》 {}", oldPath, newPath);
        this.sftp.rename(oldPath, newPath);
        logger.info("结束迁移文件：{} -》 {}", oldPath, newPath);
    }

    /**
     * 判断目录是否存在
     *
     * @param path
     * @return
     */
    public boolean isExistDir(String path) {
        boolean isExist = false;
        try {
            SftpATTRS sftpATTRS = this.sftp.stat(path);
            isExist = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            logger.error("判断目录是否存在异常", e);
            if (e.getMessage().equalsIgnoreCase("no such file")) {
                isExist = false;
            }
        }
        return isExist;

    }

    public ChannelSftp getSftp() {
        return sftp;
    }

    public void setSftp(ChannelSftp sftp) {
        this.sftp = sftp;
    }
}
