package main.java;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class ImageAdaptor implements ImgOverNotify
{
    public static final String TMP_DIR = Cfg.tmp_dir + Cfg.sep + "temp";
    public static final int MAX_IMG_SIZE = 4096 * 4096; // 16M
    public final ModelManager manager;
    private final AdaptorNotify back;
    private int imgae_idx = 0;

    static 
    {
        File tempDir = new File(TMP_DIR); 
        if (!tempDir.exists()) 
        {
            tempDir.mkdirs();
        }
    }

    public ImageAdaptor(AdaptorNotify back)
    {
        this.manager = new ModelManager(this);
        this.back = back;
    }

    public boolean cmd(ImgNicerReq req)
    {
        List<InputParam> model_req = null;
        String img_path = null;
        ProcessImgTask task = null;

        /* check if the command is valid */
        if(!req.text.startsWith("img"))
        {
            return false;
        }

        /* analyze the command */
        try
        {
            model_req = this.analyzeCommand(req.text);
        }
        catch(ImageException e)
        {
            return true;
        }

        /* show help if the cmd is -h or --help */
        if(model_req.get(0).help)
        {
            showHelp(req);
            return true;
        }

        /* check if the cmd of the model is valid */
        if(!this.manager.checkReq(model_req))
        {
            return true;
        }

        /* get the input image */
        img_path = this.back.getImgPath();

        /* check if the input image is too large before or after processing */
        try
        {
            if(isImgTooLarge(img_path, model_req))
            {
                
                this.back.callBack();
                return true;
            }
        }
        catch(ImageException e)
        {
            this.back.callBack();
            return true;
        }

        /* process the image */
        task = new ProcessImgTask();
        task.req = req;
        task.reqs = model_req;
        task.input = img_path;
        task.output = this.getOutImgPath();
        this.manager.processImage(task);

        return true;
    }

    private List<InputParam> analyzeCommand(String command) throws ImageException 
    {
        String[] cmds = null;
        List<InputParam> result = null;
        InputParam req = null;
        int i = 0;

        if(command.contains("|"))
        {
            cmds = command.split("|");
        }
        else
        {
            cmds = new String[1];
            cmds[0] = command;
        }

        result = new ArrayList<InputParam>();
        for(i = 0; i < cmds.length; i++)
        {
            req = this.getReqByCmd(cmds[i]);
            result.add(req);
        }

        return result;
    }

    private InputParam getReqByCmd(String cmd) throws ImageException
    {
        String[] args = cmd.split("\\s+");
        InputParam req = null;
        int i = 0;

        if(args.length < 1 || !"img".equals(args[0]))
        {
            throw ImageException.param_error;
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
                        throw new ImageException("Missing value for -model parameter.");
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
                                throw new ImageException("Scale value must be a positive number.");
                            }
                        } 
                        catch (NumberFormatException e) 
                        {
                            throw new ImageException("Invalid scale value. It must be a valid positive number.");
                        }
                        i++; // 跳过下一个值
                    } else {
                        throw new ImageException("Missing value for scale parameter.");
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
                            throw new ImageException("Noise value must be an integer.");
                        }
                        i++; // 跳过下一个值
                    } 
                    else 
                    {
                        throw new ImageException("Missing value for noise parameter.");
                    }
                    break;

                case "-m":
                {
                    if (i + 1 < args.length) 
                    {
                        req.mode = args[i + 1];
                        i++;
                    } 
                    else 
                    {
                        throw new ImageException("Missing value for -m parameter.");
                    }
                    break;
                }

                case "-h":
                case "--help":
                {
                    req.help = true;
                    break;
                }

                default:
                {
                    if (!arg.startsWith("-"))
                    {
                        throw new ImageException("Invalid parameter: " + arg);
                    }
                    break;
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

    @Override
    public void processError(ImageException e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processError'");
    }

    private synchronized String getOutImgPath()
    {
        this.imgae_idx++;
        return TMP_DIR + Cfg.sep + "out_" + this.imgae_idx + ".png";
    }

    private static boolean isImgTooLarge(String imgPath, List<InputParam> reqs) throws ImageException
    {
        int img_size = imgSize(imgPath);
        if(img_size > MAX_IMG_SIZE)
        {
            return true;
        }
        for(InputParam req : reqs)
        {
            if(req.scale > 0)
            {
                img_size = (int)(img_size * req.scale);
                if(img_size > MAX_IMG_SIZE)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static int imgSize(String imgPath) throws ImageException
    {
        try 
        {
            // 读取图片
            File imgFile = new File(imgPath);
            BufferedImage img = ImageIO.read(imgFile);
            
            if (img == null) {
                throw new IOException("Invalid image file.");
            }
            
            // 获取宽度和高度
            int width = img.getWidth();
            int height = img.getHeight();
            
            // 返回像素数（长*宽）
            return width * height;
        } 
        catch (IOException e) 
        {
            throw ImageException.img_error;
        }
    }

    private void showHelp(ImgNicerReq req)
    {

    }
}
