package main.java;

import main.java.manager.model.waifu.WaifuParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static main.java.cfg.Cfg.waifu2x_converter;

public class Main 
{
    public static void main(String[] args) {
        try {
            Process process = getCommand1();

            // 获取命令的标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Command Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // 打印输出的每一行
            }

            // 获取命令的错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            System.out.println("Command Error Output (if any):");
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);  // 打印错误输出
            }

            // 等待命令执行完成并获取退出状态
            int exitCode = process.waitFor();
            System.out.println("Command executed with exit code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Process getCommand1() throws IOException {
        String[] commandArgs = {
                waifu2x_converter,
                "-i", "D:\\image\\galgame\\out\\EV112AA.png",  // 输入文件路径
                "-o", "D:\\temp\\g.png",                      // 输出文件路径
                "-f", "png",                                  // 输出格式
                "-c", "9",                                    // PNG 压缩级别
                "-m", "noise-scale",                          // 处理模式
                "--scale-ratio", "0.2",                         // 放大倍数
                "--noise-level", "1"                          // 噪声级别
        };

        // 创建 ProcessBuilder 对象并传入命令和参数
        ProcessBuilder processBuilder = new ProcessBuilder(commandArgs);
        processBuilder.directory(new java.io.File("D:\\code\\Projects\\java\\image\\image\\src\\main\\resource\\model\\"));  // 设置工作目录

        // 启动进程
        Process process = processBuilder.start();
        return process;
    }

    private static List<String> getCommand(WaifuParam param) throws ImageException
    {
        List<String> command = null;
        int noise = 1;
        int scale = 2;
        int tmp = 0;

        if(!new File(param.input).isFile())
        {
            throw ImageException.param_error;
        }

        switch (param.mode)
        {
            case WaifuParam.mode_scale_noise -> tmp |= noise | scale;
            case WaifuParam.mode_noise -> tmp |= noise;
            case WaifuParam.mode_scale -> tmp |=  scale;
            default -> throw ImageException.mode_error;
        }
        
        if((tmp & noise) != 0)
        {
            if(param.noise > WaifuParam.max_noise || param.noise < WaifuParam.min_noise)
            {
                throw ImageException.noise_error;
            }
        }
        if((tmp & scale) != 0)
        {
            if(param.noise > WaifuParam.max_scale || param.noise < WaifuParam.min_scale)
            {
                throw ImageException.scale_error;
            }
        }

        command = new ArrayList<>();
        command.add("-i");
        command.add(param.input);
        command.add("-o");
        command.add(param.output);
        command.add("-f");
        command.add(param.format);
        command.add("-m");
        command.add(param.mode);

        if((tmp & scale) != 0)
        {
            command.add("--scale-ratio");
            command.add(String.valueOf(param.scale));
        }
        if((tmp & noise) != 0)
        {
            command.add("--noise-level");
            command.add(String.valueOf(param.noise));
        }
        
        return command;
    }
}

