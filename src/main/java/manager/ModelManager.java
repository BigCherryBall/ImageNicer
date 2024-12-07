package main.java.manager;

import main.java.ImageException;
import main.java.adaptor.InputParam;
import main.java.exception.ProcessImgTask;
import main.java.manager.model.ImageModel;
import main.java.manager.model.waifu.Waifu;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class ModelManager implements Runnable
{
    private final ImgOverNotify back;
    private final List<ImageModel> models;
    private final BlockingQueue<ProcessImgTask> queue;
    private ImageModel default_model;
    
    public ModelManager(ImgOverNotify back)
    {
        this.back = back;
        this.queue = new LinkedBlockingQueue<>(4);
        models = List.of
        (
            Waifu.getInstance()
        );

        this.default_model = models.get(0);
        Thread thread = new Thread(this);
        thread.start();
    }

    public boolean checkReq(List<InputParam> reqs) throws ImageException
    {
        for (InputParam req : reqs) 
        {
            if(!this.checkInput(req))
            {
                return false;
            }
        }
        
        return true;
    }

    public void processImage(ProcessImgTask task)
    {
        try 
        {
            queue.put(task);
            System.out.println("ModelManager: " + task.input + " added to queue");
        } 
        catch (InterruptedException e) 
        {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() 
    {
        while (true) 
        {
            ProcessImgTask task;
            String last_img;
            String output_img;
            int cmd_size = 0;
            int count = 0;
            boolean error = false;
            try
            {
                task = this.queue.take();
            }
            catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }

            last_img = task.input;
            cmd_size = task.reqs.size();

            for(InputParam req : task.reqs)
            {
                count++;
                if(count == cmd_size)
                {
                    output_img = task.output;
                }
                else
                {
                    output_img = task.output + "_" + count + ".png";
                }
                for (ImageModel model : this.models)
                {
                    if(!model.isMe(req.model))
                    {
                        continue;
                    }

                    try
                    {
                        model.processImage(req, last_img, output_img);
                        last_img = output_img;
                    }
                    catch(ImageException e)
                    {
                        this.back.processError(task.req, e);
                        error = true;
                    }
                    
                    break;
                }

                if(error)
                {
                    break;
                }
            }

            if(error)
            {
                continue;
            }
            
            this.back.processOver(task.req, last_img);
        }
        
    }

    private boolean checkInput(InputParam input) throws ImageException
    {
        int scale = 1;
        int noise = 2;
        int tmp = 0;
        boolean model_checked = false;
        boolean mode_checked = false;

        if(input.model == null || input.model.isEmpty())
        {
            input.model = this.default_model.getName();
            model_checked = true;
        }
        else
        {
            for (ImageModel model : this.models) 
            {
                if(!model.isMe(input.model))
                {
                    continue;
                }

                model_checked = true;
            }
        }
        if(!model_checked)
        {
            throw ImageException.mode_error;
        }

        if(input.mode == null || input.mode.isEmpty())
        {
            input.scale = 1;
            input.noise = 0;
            input.mode = "";
            mode_checked = true;
        }
        else
        {
            
            if(input.mode.equals("s"))
            {
                tmp = scale;
            }
            else if(input.mode.equals("n"))
            {
                tmp = noise;
            }
            else if(input.mode.equals("sn") || input.mode.equals("ns"))
            {
                tmp = scale | noise;
            }
            else
            {
                throw ImageException.mode_error;
            }

            if((tmp & scale) == scale)
            {
                if(input.scale < 0.1f || input.scale > 10f)
                {
                    throw ImageException.scale_error;
                }
                mode_checked = true;
            }

            if((tmp & noise) == noise)
            {
                if(input.noise < 0 || input.noise > 4)
                {
                    throw ImageException.noise_error;
                }
                mode_checked = true;
            }
        }
        if(!mode_checked)
        {
            throw ImageException.mode_error;
        }

        return true;
    }
}

