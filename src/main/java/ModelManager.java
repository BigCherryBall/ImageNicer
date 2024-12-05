package main.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class ModelManager implements Runnable
{
    private final ImgOverNotify back;
    private final List<ImageModel> models;
    private final BlockingQueue<ProcessImgTask> queue;
    
    public ModelManager(ImgOverNotify back)
    {
        this.back = back;
        this.queue = new LinkedBlockingQueue<>(4);
        models = List.of
        (
            Waifu.instance
        );
    }

    public boolean checkReq(List<InputParam> reqs)
    {
        boolean checked = false;

        for (InputParam req : reqs) 
        {
            checked = false;
            for (ImageModel model : this.models) 
            {
                if(!model.isMe(req.model))
                {
                    continue;
                }

                if(model.checkCmd(req))
                {
                    checked = true;
                }
                else
                {
                    return false;
                }
            }

            if(!checked)
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
            output_img = task.output;
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
                        this.back.processError(e);
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
}

