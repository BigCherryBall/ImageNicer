package main.java;

public final class WaifuParam
{
    /* the image that need handle, please use absolute path */
    public String input;

    /* output image file */
    public String output;


    /* output image format */
    public static final String format_png = "png";
    public static final String format_jpg = "jpg";
    public static final String format_webp = "webp";
    public String format;

    /* handle mode */
    public static final String mode_scale = "scale";
    public static final String mode_noise = "noise";
    public static final String mode_scale_noise = "noise-scale";
    public String mode;

    /* if the param named mode that you select scale or noise-scale, you need scale > 0f */
    public static final float min_scale = 0.1f;
    public static final float max_scale = 10f;
    public float scale;

    /* if the param named mode that you select noise or noise-scale, you need noise [1, 4] */
    public static final int min_noise = 1;
    public static final int max_noise = 4;
    public int noise;
}
