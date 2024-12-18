package main.java.manager.model.waifu;

import main.java.cfg.Cfg;
import main.java.ImageException;
import main.java.adaptor.InputParam;
import main.java.manager.model.ImageModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public final class Waifu extends ImageModel
{
    private static Waifu instance;
    private static final String resourceDir;
    private static final File waifu_dir;
    private static final String waifu_exe_file_path;

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> futureTask = null;

    //private static final String exe = "D:\\code\\Projects\\java\\image\\image\\src\\main\\resource\\model\\waifu\\waifu2x-converter-cpp_waifu2xEX.exe";
    static 
    {
        resourceDir = "model" + Cfg.sep + "waifu";
        String waifu_dir_path = Cfg.tmp_dir + "model" + Cfg.sep + "waifu" + Cfg.sep;
        waifu_dir = new File(waifu_dir_path);
        waifu_exe_file_path = waifu_dir_path + "waifu2x-converter-cpp_waifu2xEX.exe";
        if (!waifu_dir.exists()) 
        {
            waifu_dir.mkdirs();
        }
        try 
        {
            System.out.println("resourceDir=%s, waifu_dir=%s".formatted(resourceDir, waifu_dir_path));
            extractResources(resourceDir, waifu_dir);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    private Waifu()
    {
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public String processImage(InputParam req, String input, String output) throws ImageException
    {

        List<String> cmdList = this.getCmdList(req, input, output);
        System.out.println("cmd list:" + cmdList.toString() );
        ProcessBuilder processBuilder = new ProcessBuilder(cmdList);
        processBuilder.inheritIO();
        processBuilder.directory(waifu_dir);
        try
        {
            Process process = processBuilder.start();
            if(this.futureTask != null && !this.futureTask.isDone())
            {
                this.futureTask.cancel(true);
            }
            this.futureTask = scheduler.schedule(process::destroy, 30L, java.util.concurrent.TimeUnit.SECONDS);
            int exitCode = process.waitFor();
            System.out.println("Command executed with exit code: " + exitCode);
            if(!futureTask.isDone())
            {
                this.futureTask.cancel(false);
            }
            if(exitCode!= 0)
            {
                throw ImageException.process_error;
            }
        }
        catch(IOException | InterruptedException e)
        {
            throw ImageException.process_error;
        }



        
        return output;
    }

    @Override
    public boolean isMe(String name) 
    {
        return name.equalsIgnoreCase("w") || name.equalsIgnoreCase("waifu");
    }

    @Override
    public boolean checkCmd(InputParam req) 
    {
        return true;
    }

    private List<String> getCmdList(InputParam req, String input, String output) throws ImageException
    {
        WaifuParam param = new WaifuParam();
        param.input = input;
        param.output = output;
        switch (req.mode) 
        {
            case "n" -> param.mode = WaifuParam.mode_noise;
            case "s" -> param.mode = WaifuParam.mode_scale;
            case "ns", "sn" -> param.mode = WaifuParam.mode_scale_noise;
            default-> throw ImageException.mode_error;
        }
        param.noise = req.noise;
        param.scale = req.scale;
        param.format = WaifuParam.format_png;
        return this.getCommand(param);
    }

    private List<String> getCommand(WaifuParam param) throws ImageException
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
            case WaifuParam.mode_scale -> tmp |= scale;
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
            if(param.scale > WaifuParam.max_scale || param.scale < WaifuParam.min_scale)
            {
                throw ImageException.scale_error;
            }
        }

        command = new ArrayList<>();
        command.add(waifu_exe_file_path);
        command.add("-i");
        command.add(param.input);
        command.add("-o");
        command.add(param.output);
        command.add("-f");
        command.add(param.format);
        if(param.format.equals(WaifuParam.format_jpg))
        {
            command.add("-q");
            command.add("101");
        }
        else if(param.format.equals(WaifuParam.format_png))
        {
            command.add("-c");
            command.add("9");
        }
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

    @Override
    public String getName()
    {
        return "waifu";
    }

    public static Waifu getInstance()
    {
        if(instance == null)
        {
            instance = new Waifu();
        }
        return instance;
    }




}
    
