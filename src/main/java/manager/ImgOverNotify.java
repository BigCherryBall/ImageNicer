package main.java.manager;

import main.java.ImageException;
import main.java.adaptor.ImgNicerReq;

public interface ImgOverNotify
{
    void processOver(ImgNicerReq req, String output);

    void processError(ImgNicerReq req, ImageException e);
}
