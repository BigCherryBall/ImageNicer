package main.java.adaptor;
public interface AdaptorNotify
{
    void callBack(ImgNicerReq originalReq, String response);
    String getImgPath(ImgNicerReq originalReq);
    
}