package main.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Waifu extends ImageModel
{
    public static final Waifu instance = new Waifu();

    @Override
    public String processImage(InputParam req, String input, String output)
    {
        return "";
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
        param.mode = req.mode;
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
