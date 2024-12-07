package main.java.adaptor;
public interface AdaptorNotify
{
    void callBack(ImgNicerReq originalReq, int statusCode, String response);
    String getImgPath(ImgNicerReq originalReq);
    
}