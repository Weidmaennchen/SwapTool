package de.weidmaennchen.swaptool;

import android.graphics.Bitmap;

import java.io.File;

public class Wallpaper {

    private File file;
    private Bitmap bitmap;

    public File getFile()
    {
        return file;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }

    public Wallpaper(File file, Bitmap bitmap)
    {
        this.file = file;
        this.bitmap = bitmap;
    }

    public void moveToDirectory(File dest)
    {
        File destfile = new File(dest.getPath() + "/" +  file.getName());
        boolean success = file.renameTo(destfile);
        file = destfile;
    }
}
