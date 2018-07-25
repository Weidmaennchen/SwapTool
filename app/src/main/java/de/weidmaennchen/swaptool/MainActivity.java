package de.weidmaennchen.swaptool;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main activity for this app
 */
public class MainActivity extends AppCompatActivity {

    private Bitmap currentBitmap;
    private AtomicBoolean saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        saving = new AtomicBoolean(false);

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SET_WALLPAPER};
        ActivityCompat.requestPermissions(this,permissions,1);

        addListenerOnButtons();
    }

    /**
     * Adds listeners to the swap and the save button
     */
    private void addListenerOnButtons() {

        final ImageButton swapButton = findViewById(R.id.swapButton);
        swapButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new WallpaperSwapTask().execute(null,null,null);
            }
        });

        final Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(!saving.get())
                {
                    new WallpaperSaveTask().execute(null, null, null);
                }
            }
        });
    }

    /**
     * A tasks that sets the wallpaper from {@link MainActivity#currentBitmap} on the device.
     * Also disables the save button before doing so.
     */
    private class WallpaperSaveTask extends AsyncTask<Void,Void,Void>
    {
        Button saveButton;

        @Override
        protected void onPreExecute()
        {
            saveButton = findViewById(R.id.saveButton);
            saving.set(true);
            saveButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            WallpaperSwapper.SwapToBitmap(currentBitmap, getBaseContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            saving.set(false);
        }
    }

    /**
     * Gets a new random wallpaper and sets the backround of the main layout to it.
     * Also disables buttons before, starts animation and enables them after.
     */
    private class WallpaperSwapTask extends AsyncTask<Void,Void,Bitmap>
    {
        ImageButton swapButton;
        Button saveButton;
        Animation spinAnimation;
        ConstraintLayout mainLayout;

        @Override
        protected void onPreExecute()
        {
            swapButton = findViewById(R.id.swapButton);
            swapButton.setEnabled(false);

            saveButton = findViewById(R.id.saveButton);
            saveButton.setEnabled(false);

            spinAnimation = AnimationUtils.loadAnimation(getBaseContext(),R.anim.rotate_around_center);
            mainLayout = findViewById(R.id.mainLayout);

            swapButton.startAnimation(spinAnimation);
            mainLayout.setBackgroundColor(Color.BLACK);

            if(currentBitmap != null)
            {
                currentBitmap.recycle();
            }
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Wallpapers");
            File[] candidates = path.listFiles();

            return WallpaperSwapper.getRandomWallpaper(candidates, getBaseContext());
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            mainLayout.setBackground(new BitmapDrawable(getResources(), result));
            currentBitmap = result;

            swapButton.clearAnimation();

            swapButton.setEnabled(true);
            saveButton.setEnabled(true);
        }
    }
}
