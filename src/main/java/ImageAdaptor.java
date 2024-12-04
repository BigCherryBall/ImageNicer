package main.java;

import java.util.ArrayList;
import java.util.List;

public final class ImageAdaptor implements ImgOverNotify
{
    public final ModelManager manager;
    private final AdaptorNotify back;

    public ImageAdaptor(AdaptorNotify back)
    {
        this.manager = new ModelManager(this);
        this.back = back;
    }

    public boolean cmd(ImgNicerReq req)
    {
        List<InputParam> model_req = null;
        String img_path = null;

        if(!req.text.startsWith("img"))
        {
            return false;
        }

        try
        {
            model_req = this.analyzeCommand(req.text);
        }
        catch(Exception e)
        {
            return true;
        }

        if(!this.manager.checkReq(model_req))
        {
            return true;
        }

        img_path = this.back.getImgPath();

        return true;
    }

    private List<InputParam> analyzeCommand(String command) throws Exception 
    {
        String[] cmds = null;
        List<InputParam> result = null;
        InputParam req = null;
        int i = 0;

        if(command == null)
        {
            throw new Exception("Missing value for -model parameter.");
        }

        if(command.contains("|"))
        {
            cmds = command.split("|");
        }
        else
        {
            cmds = new String[1];
            cmds[0] = command;
        }

        if(cmds.length == 0)
        {
            throw new Exception("Missing value for -model parameter.");
        }

        result = new ArrayList<InputParam>();
        for(i = 0; i < cmds.length; i++)
        {
            req = this.getReqByCmd(cmds[i]);
            result.add(req);
        }

        return result;
    }

    private InputParam getReqByCmd(String cmd) throws Exception
    {
        String[] args = cmd.split("\\s+");
        InputParam req = null;
        int i = 0;

        if(args.length < 1 || !"img".equals(args[0]))
        {
            throw new Exception("Missing value for -model parameter.");
        }

        req = new InputParam();
        for (i = 1; i < args.length; i++) 
        {
            String arg = args[i];

            switch (arg) 
            {
                case "-model":
                    if (i + 1 < args.length) 
                    {
                        req.model = args[i + 1];
                        i++;
                    }
                    else 
                    {
                        throw new Exception("Missing value for -model parameter.");
                    }
                    break;

                case "-s":
                case "--scale":
                    if (i + 1 < args.length) 
                    {
                        try 
                        {
                            req.scale = Float.parseFloat(args[i + 1]);
                            if (req.scale <= 0) 
                            {
                                throw new Exception("Scale value must be a positive number.");
                            }
                        } 
                        catch (NumberFormatException e) 
                        {
                            throw new Exception("Invalid scale value. It must be a valid positive number.");
                        }
                        i++; // 跳过下一个值
                    } else {
                        throw new Exception("Missing value for scale parameter.");
                    }
                    break;

                case "-n":
                case "--noise":
                    if (i + 1 < args.length) 
                    {
                        try 
                        {
                            req.noise = Integer.parseInt(args[i + 1]);
                        } 
                        catch (NumberFormatException e) 
                        {
                            throw new Exception("Noise value must be an integer.");
                        }
                        i++; // 跳过下一个值
                    } 
                    else 
                    {
                        throw new Exception("Missing value for noise parameter.");
                    }
                    break;

                case "-m":
                    if (i + 1 < args.length) 
                    {
                        req.mode = args[i + 1];
                        i++;
                    } 
                    else 
                    {
                        throw new Exception("Missing value for -m parameter.");
                    }
                    break;

                default:
                    if (!arg.startsWith("-"))
                    {
                        throw new Exception("Invalid parameter: " + arg);
                    }
            }
        }
        return req;
    }

    public static void main(String[] args) {
        try {
            String command = "img -model w -s 1.5 -d -n 2 -m noise_scale";
            InputParam result = new InputParam();
            System.out.println("Model: " + result.model);
            System.out.println("Scale: " + result.scale);
            System.out.println("Noise: " + result.noise);
            System.out.println("Mode: " + result.mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processOver(ImgNicerReq req, String output)
    {

    }
}
