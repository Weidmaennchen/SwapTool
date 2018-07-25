package de.weidmaennchen.swaptool;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The WallpaperSwapper can create and swap wallpapers on the device.
 */
public class WallpaperSwapper {

    /**
     * Short convenience method to swap the current wallpaper to a random one
     * @param context current context
     * @throws FileNotFoundException if there is no file
     */
    public static synchronized void RandomSwap(Context context) {
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Wallpapers");
        File[] candidates = path.listFiles();

        Bitmap rawBitmap = getRandomWallpaper(candidates, context);
        SwapToBitmap(rawBitmap, context);
    }

    /**
     * gets a random wallpaper from a list of candidate files (it has the size of the screen)
     * @param candidates a list of files that contain images which can be decoded as a bitmap
     * @param context current context
     * @return a wallpaper sized as big as the screen
     */
    public static Bitmap getRandomWallpaper(File[] candidates, Context context)
    {
        Point size = getScreensizePoint(context);

        int index = randomInt(0,candidates.length-1);
        String newWallpaper = candidates[index].getAbsolutePath();
        if(candidates[index].exists())
        {
            Bitmap rawBitmap = decodeSampledBitmapFromFile(newWallpaper,size.x,size.y);
            return getScaledDownBitmap(rawBitmap,size.x,false);
        }
        return null;
    }

    /**
     * Changes the current device wallpaper to a given bitmap
     * @param wallpaperBitmap the bitmap to be used as wallpaper
     * @param context context
     */
    public static void SwapToBitmap(Bitmap wallpaperBitmap, Context context)
    {
        try {

            if (Build.VERSION.SDK_INT >= 24){
                WallpaperManager.getInstance(context).setBitmap(wallpaperBitmap,null,true,WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
            } else{
                WallpaperManager.getInstance(context).setBitmap(wallpaperBitmap);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
        Returns a random int between min (inclusive) and max (inclusive)
     */
    private static int randomInt(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Calculates a multiple of 2 that is the minimal one being bigger than a given height and width
     * @param options containing outHeight and outWidth of the bitmap which needs to be sampled
     * @param reqWidth minimum width to be surpassed
     * @param reqHeight minimum height to be surpassed
     * @return a multiple of 2 to sample
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * decodes a sampled bimap from a file
     * @param filename file containing an image
     * @param reqWidth minimum width to be surpassed
     * @param reqHeight minimum height to be surpassed
     * @return a sampled bitmap from the input file
     * @throws FileNotFoundException if no file found
     */
    private static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Gets a scaled down bitmap to a threshold. The threshold will be the new width or height, whichever is the smaller one.
     * @param bitmap the original bitmap you need a smaller version of
     * @param threshold the new width or height, whichever is the smaller one.
     * @param isNecessaryToKeepOrig set false if input bitmap can be recycled
     * @return a scaled down bitmap
     */
    private static Bitmap getScaledDownBitmap(Bitmap bitmap, int threshold, boolean isNecessaryToKeepOrig){
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
            //noinspection SuspiciousNameCombination
            newHeight = newWidth;
        }

        if(width == height && width <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        return getResizedBitmap(bitmap, newWidth, newHeight, isNecessaryToKeepOrig);
    }

    /**
     * resizes a bitmap to a width and height
     * @param bm the original bitmap
     * @param newWidth the new width to resize to.
     * @param newHeight the new height to resize to
     * @param isNecessaryToKeepOrig true if the input bitmap can be recycled. if set to true, it will be
     * @return a resized bitmap
     */
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

    /**
     * gets a point with x and y referring to width and height of the screen.
     * @param context the context
     * @return point containing x and y as big as width and height of the screen
     */
    private static Point getScreensizePoint(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size;
    }

}
