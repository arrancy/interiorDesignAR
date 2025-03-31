package com.example.modernfurniture;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.PixelCopy;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class camera extends AppCompatActivity {
    private ArFragment arFragment; // Declare at class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        ArSceneView sceneView = arFragment.getArSceneView();
        Button captureButton = findViewById(R.id.buttonCapture);

        // Set a click listener on the button to trigger photo capture
        captureButton.setOnClickListener(v -> takePhoto());
    }

    private void takePhoto() {
        if (arFragment == null || arFragment.getArSceneView() == null) {
            Toast.makeText(this, "AR Fragment not initialized!", Toast.LENGTH_SHORT).show();
            return;
        }

        final ArSceneView view = arFragment.getArSceneView();
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                                                   Bitmap.Config.ARGB_8888);

        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        PixelCopy.request(view, bitmap, copyResult -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Failed to copyPixels: " + copyResult,
                               Toast.LENGTH_SHORT).show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    private void saveBitmapToDisk(Bitmap bitmap) throws IOException {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!picturesDir.exists()) {
            picturesDir.mkdirs();
        }
        String filename = "AR_Capture_" + System.currentTimeMillis() + ".png";
        File outFile = new File(picturesDir, filename);

        FileOutputStream outputStream = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.flush();
        outputStream.close();

        MediaScannerConnection.scanFile(this,
                new String[]{ outFile.getAbsolutePath() },
                null, null);

        Toast.makeText(this, "Saved: " + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }
}