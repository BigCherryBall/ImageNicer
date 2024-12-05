package main.java;

public interface ImgOverNotify
{
    void processOver(ImgNicerReq req, String output);

    void processError(ImageException e);
}
