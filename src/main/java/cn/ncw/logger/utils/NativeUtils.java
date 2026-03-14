package cn.ncw.logger.utils;

import java.io.*;
import java.nio.file.*;

/**
 * 从JAR中提取本地库并加载的工具类。
 */
public class NativeUtils {

    /**
     * 从JAR中提取指定资源到临时文件并加载。
     *
     * @param resourcePath JAR中的资源路径，例如 "/bluescreen.dll"
     * @throws IOException 如果提取失败
     */
    public static void loadLibraryFromJar(String resourcePath) throws IOException {
        // 获取资源输入流
        try (InputStream in = NativeUtils.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("资源不存在: " + resourcePath);
            }

            // 创建临时文件，文件名使用前缀+后缀，并确保唯一性
            String libName = new File(resourcePath).getName();
            int dotIndex = libName.lastIndexOf('.');
            String prefix = dotIndex > 0 ? libName.substring(0, dotIndex) : libName;
            String suffix = dotIndex > 0 ? libName.substring(dotIndex) : ".dll";

            File tempFile = File.createTempFile(prefix + "_", suffix);
            tempFile.deleteOnExit(); // JVM退出时删除

            // 将资源内容复制到临时文件
            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            // 加载临时文件
            System.load(tempFile.getAbsolutePath());
        }
    }

    /**
     * 按架构选择正确的DLL并从JAR加载。
     * 假设JAR中有 /native/windows-x86_64/bluescreen.dll 和 /native/windows-x86/bluescreen.dll
     */
    public static void loadLibraryForArch() throws IOException {
        String arch = System.getProperty("os.arch").toLowerCase();
        String libPath;
        if (arch.contains("64")) {
            libPath = "/native/windows-x86_64/bluescreen.dll";
        } else {
            libPath = "/native/windows-x86/bluescreen.dll";
        }
        loadLibraryFromJar(libPath);
    }
}
