package de.rdlgrmpf.scribblecam;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.simplify.ink.InkView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import petrov.kristiyan.colorpicker.ColorPicker;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    InkView inkView;
    int backgroundColor;
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_EXTERNAL_STORAGE);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_EXTERNAL_STORAGE);
            }
        } else {

        }

        if( getIntent() != null && getIntent().getExtras() != null ) {
            fileUri = getIntent().getExtras().getParcelable( MediaStore.EXTRA_OUTPUT );
        }

        inkView = (InkView) findViewById(R.id.inkview);
        backgroundColor = getResources().getColor(android.R.color.white);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Do nothing

                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_need_permission_write_external, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_draw_width:
                menuDrawWidth();
                break;
            case R.id.action_draw_color:
                menuDrawColor();
                break;
            case R.id.action_color_fill:
                menuColorFill();
                break;
            case R.id.action_done:
                menuDone();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void menuDrawWidth(){

    }
    private void menuDrawColor(){
        displayColorDialog(false);
    }
    private void menuColorFill(){
        displayColorDialog(true);
    }
    private void menuDone(){
        Bitmap bitmap = inkView.getBitmap(backgroundColor);
        try {
            if (fileUri != null) {
                OutputStream os = getContentResolver().openOutputStream(fileUri);
                //File file = new File(mFilePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(getBaseContext(), "kein fileUri", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
        }
    }

    private void displayColorDialog(final boolean isFill) {
        new ColorPicker(this)
                .setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                    @Override
                    public void setOnFastChooseColorListener(int position, int color) {
                        if (isFill) {
                            inkView.setBackgroundColor(color);
                            backgroundColor = color;
                        } else {
                            inkView.setColor(color);
                        }
                    }
                })
                .setColors(R.array.all_material)
                .setTitle(isFill ? getString(R.string.pick_fill_color): getString(R.string.pick_draw_color))
                .show();
    }

    //taken from http://stackoverflow.com/a/10616868
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }
}
