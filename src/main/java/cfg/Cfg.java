package main.java.cfg;

import java.io.File;

public final class Cfg
{
    public static final String pwd = System.getProperty("user.dir");
    public static final String tmp_dir = System.getProperty("java.io.tmpdir");
    public static final String sep = File.separator;
    public static final String res = pwd + sep + "src" + sep + "main" + sep + "resource";
    public static final String waifu2x_converter = res + sep + "model" + sep + "waifu2x-converter-cpp_waifu2xEX.exe";

}
