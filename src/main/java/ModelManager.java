package main.java;

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
        for (InputParam req : reqs) 
        {
            for (ImageModel model : this.models) 
            {
                if(!model.isMe(req.model))
                {
                    continue;
                }

                if(!model.checkCmd(req))
                {
                    return false;
                }
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

            for(InputParam req : task.reqs)
            {
                for (ImageModel model : this.models)
                {
                    if(!model.isMe(req.model))
                    {
                        continue;
                    }

                    last_img = model.processImage(req, last_img, task.output);
                    break;
                }
            }
            this.back.processOver(task.req, last_img);
        }
        
    }
}

