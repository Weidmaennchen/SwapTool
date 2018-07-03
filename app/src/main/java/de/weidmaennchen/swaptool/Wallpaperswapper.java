package de.weidmaennchen.swaptool;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Wallpaperswapper {

    public static void Swap(Context context)
    {
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Wallpapers");
        File[] candidates = path.listFiles();
        int index = randomInt(0,candidates.length-1);
        File newWallpaper = candidates[index];
        if(newWallpaper.exists()) {
            Bitmap myBitmap = decodeFile(newWallpaper);
            myBitmap = getScaledDownBitmap(myBitmap, 1080, false);
            try {

                if (Build.VERSION.SDK_INT >= 24){
                    WallpaperManager.getInstance(context).setBitmap(myBitmap,null,true,WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
                } else{
                    WallpaperManager.getInstance(context).setBitmap(myBitmap);
                }

                //ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
                //BitmapDrawable bmdraw = new BitmapDrawable(getResources(), myBitmap);
                //mainLayout.setBackground(bmdraw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Bitmap decodeFile(File f){
        try {
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inScaled=true;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }


    /**
        Returns a random int between min (inclusive) and max (inclusive)
     */
    private static int randomInt(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * @param bitmap the Bitmap to be scaled
     * @param threshold the maxium dimension (either width or height) of the scaled bitmap
     * @param isNecessaryToKeepOrig is it necessary to keep the original bitmap? If not recycle the original bitmap to prevent memory leak.
     * */

    public static Bitmap getScaledDownBitmap(Bitmap bitmap, int threshold, boolean isNecessaryToKeepOrig){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = width;
        int newHeight = height;

        if(width > height && width > threshold){
            newWidth = threshold;
            newHeight = (int)(height * (float)newWidth/width);
        }

        if(width > height && width <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        if(width < height && height > threshold){
            newHeight = threshold;
            newWidth = (int)(width * (float)newHeight/height);
        }

        if(width < height && height <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        if(width == height && width > threshold){
            newWidth = threshold;
            newHeight = newWidth;
        }

        if(width == height && width <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        return getResizedBitmap(bitmap, newWidth, newHeight, isNecessaryToKeepOrig);
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, boolean isNecessaryToKeepOrig) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        if(!isNecessaryToKeepOrig){
            bm.recycle();
        }
        return resizedBitmap;
    }
}
