package main.java;

public abstract class ImageModel
{
    public abstract boolean checkCmd(InputParam req);

    public abstract String processImage(InputParam req, String input, String output);

    public abstract boolean isMe(String name);
}
