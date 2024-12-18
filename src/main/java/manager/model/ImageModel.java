package main.java.manager.model;

import main.java.cfg.Cfg;
import main.java.ImageException;
import main.java.adaptor.InputParam;
import main.java.manager.ModelManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

public abstract class ImageModel
{
    public abstract boolean checkCmd(InputParam req);

    public abstract String processImage(InputParam req, String input, String output) throws ImageException;

    public abstract boolean isMe(String name);

    public abstract String getName();

    protected static void extractResources(String resourceDir, File targetDir) throws IOException
    {
        // 获取 JAR 文件中该路径下所有资源
        Enumeration<URL> resources = ModelManager.class.getClassLoader().getResources(resourceDir);

        System.out.println("class path:" + ModelManager.class.getClassLoader().getResource(resourceDir+"/test.txt").getPath());

        while (resources.hasMoreElements())
        {

            URL resourceUrl = resources.nextElement();
            System.out.println("url=" + resourceUrl.getFile().toString());
            File resourceFile = new File(resourceUrl.getFile());

            // 如果是目录，递归调用
            if (resourceFile.isDirectory()) 
            {
                File targetSubDir = new File(targetDir, resourceFile.getName());
                if (!targetSubDir.exists()) {
                    targetSubDir.mkdirs();
                }
                // 递归提取子目录中的文件
                extractResources(resourceDir + Cfg.sep + resourceFile.getName(), targetSubDir);
            } 
            else 
            {
                // 如果是文件，提取该文件
                try (InputStream inputStream = ModelManager.class.getResourceAsStream(resourceDir + Cfg.sep + resourceFile.getName())) {
                    if (inputStream != null) {
                        File targetFile = new File(targetDir, resourceFile.getName());
                        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }
            }
        }
    }
}
