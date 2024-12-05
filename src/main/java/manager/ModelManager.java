package main.java.manager;

import main.java.*;
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
    
    public ModelManager(ImgOverNotify back)
    {
        this.back = back;
        this.queue = new LinkedBlockingQueue<>(4);
        models = List.of
        (
            Waifu.instance
        );
        Thread thread = new Thread(this);
        thread.start();
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
}
