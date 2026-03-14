package cn.ncw.logger.utils;

import java.io.*;
import java.nio.file.*;

/**
 * 从JAR中提取并执行批处理文件
 */
public class BatExecutor {

    /**
     * 从JAR中提取指定资源到临时文件并执行
     *
     * @param resourcePath JAR中的资源路径，例如 "/scripts/example.bat"
     * @throws IOException  IO异常
     * @throws InterruptedException 进程中断
     */
    public static void executeBatFromJar(String resourcePath) throws IOException, InterruptedException {
        // 1. 检查操作系统
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            throw new UnsupportedOperationException("批处理只能在Windows上执行");
        }

        // 2. 从JAR读取资源
        try (InputStream in = BatExecutor.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("资源不存在: " + resourcePath);
            }

            // 3. 创建临时文件（保留.bat后缀）
            String fileName = new File(resourcePath).getName();
            Path tempFile = Files.createTempFile("bat_", "_" + fileName);
            tempFile.toFile().deleteOnExit(); // JVM退出时删除

            // 4. 将资源内容复制到临时文件
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 5. 设置可执行权限（Windows上可能不需要，但为了安全）
            tempFile.toFile().setExecutable(true);

            // 6. 执行批处理文件
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", tempFile.toAbsolutePath().toString());
            pb.inheritIO(); // 将子进程的输入输出继承到当前Java进程
            Process process = pb.start();
            int exitCode = process.waitFor();

            System.out.println("批处理执行完成，退出码: " + exitCode);

            // 7. 可选：立即删除临时文件（但deleteOnExit已经保证）
            // Files.deleteIfExists(tempFile);
        }
    }
}
