package main.java;

public final class ImageException extends Exception
{
    public static final ImageException param_error = new ImageException("入参不合法");
    public static final ImageException img_error = new ImageException("图片不合法");
    public static final ImageException process_error = new ImageException("处理失败");
    public static final ImageException mode_error = new ImageException("图片模式选择错误");

    public static final ImageException scale_error = new ImageException("请选择图片的缩放倍数, 数值范围: 0.1 ~ 10");

    public static final ImageException noise_error = new ImageException("请选择图片的降噪等级, 数值范围: 1 ~ 4");
    private final String msg;

    public ImageException(String msg)
    {
        this.msg = msg;
    }

    public String getInfo()
    {
        return this.msg;
    }
}
