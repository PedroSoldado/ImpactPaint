package com.app.paintitup;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;


import java.io.File;
import java.util.UUID;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pedro.puredataplugin.Gesture;
import com.app.pedro.puredataplugin.PureData;
import com.app.pedro.puredataplugin.PureDataRecognizer;

import org.puredata.core.PdListener;


public class MainActivity extends Activity implements OnClickListener {

    private DrawingView drawView;
    private ImageButton currPaint,drawBtn, eraseBtn, newBtn, saveBtn;
    private float smallBrush, mediumBrush, largeBrush;

    public static PureDataRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognizer = new PureDataRecognizer(getApplicationContext());

        initPaint();


        recognizer.getPD().getMyDispatcher().addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {
                String bonkOutput;

                int template = (int) Double.parseDouble(objects[0].toString());
                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());
                float loudness = Float.parseFloat(objects[3].toString());
                float sigmund = Float.parseFloat(objects[4].toString());

                recognizer.addHit(template, velocity, colorTemperature, loudness);

/*                if (recognizer.detectHit() != null) {

                    Log.e("YEY", "YAY");
                }*/
            }


        });
    }

    private void initPaint() {
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        drawView = (DrawingView)findViewById(R.id.drawing);

        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);
        drawView.setBrushSize(mediumBrush);

        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);

        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
    }

    boolean firstTouch = true;
    public boolean dispatchTouchEvent (MotionEvent event){
        super.dispatchTouchEvent(event);
        int x = (int)event.getX();
        int y = (int)event.getY();

        if(firstTouch) {
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/results");
            File namesFile = new File(dir, "namesMapping.txt");
            File templateFile = new File(dir, "templates.txt");
            File intensityFile = new File(dir, "intensityResults.txt");

            recognizer.importFiles(namesFile, templateFile, intensityFile);
            firstTouch = false;
        }

        if(event.getAction() == MotionEvent.ACTION_UP){
            recognizer.addTouch(x,y);
        }

        //Gesture g = recognizer.detectHit();
        String gesture = recognizer.getGesture().toUpperCase();
        Log.e("Gesture: ", gesture);

        TextView text = (TextView) findViewById(R.id.gestureView);
        if(!("NULL".equals(gesture)))
            text.setText(gesture);

        switch(gesture){
            case "TAP": drawView.setColor("#000000"); break;
            case "KNOCK":
                
                //drawView.setColor("#FFFFFFFF");
                break;
            case "NAIL": drawView.setColor("#FFFF00"); break;
        }

        //Log.e("Gesture detected:", gesture);
        /*if(g != null){
            g.
            Log.e("DETECTEEEED", "HERE");
        }*/

        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void paintClicked(View view){
        //use chosen color
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());
        if(view!=currPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            //update color

            drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }
    }

    @Override
    public void onClick(View v) {

        Dialog brushDialog;

        switch(v.getId()){
            case R.id.draw_btn:
                toggleButtons();
                v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                brushDialog = new Dialog(this);
                brushDialog.setTitle("Brush size:");
                //draw button clicked
                brushDialog.setContentView(R.layout.brush_chooser);

                listenForClicksSizes(brushDialog);
                brushDialog.show();
                break;
            case R.id.erase_btn:
                toggleButtons();
                v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                //switch to erase - choose size
                brushDialog = new Dialog(this);
                brushDialog.setTitle("Eraser size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                listenForClicksErase(brushDialog);
                brushDialog.show();
                break;
            case R.id.new_btn:

                //button.setImageDrawable(getResources().getDrawable(R.drawable.feature_pressed));

                drawView.startNew();
                break;
            case R.id.save_btn:
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
                saveDialog.setTitle("Save drawing");
                saveDialog.setMessage("Save drawing to device Gallery?");
                saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        //save drawing
                    }
                });
                saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
                saveDialog.show();

                drawView.setDrawingCacheEnabled(true);

                String imgSaved = MediaStore.Images.Media.insertImage(
                        getContentResolver(), drawView.getDrawingCache(),
                        UUID.randomUUID().toString()+".png", "drawing");

                if(imgSaved!=null){
                    Toast savedToast = Toast.makeText(getApplicationContext(),
                            "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                    savedToast.show();
                }
                else{
                    Toast unsavedToast = Toast.makeText(getApplicationContext(),
                            "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                    unsavedToast.show();
                }

                drawView.destroyDrawingCache();
                break;
        }

    }

    private void toggleButtons() {
        LinearLayout featureLayout = (LinearLayout) findViewById(R.id.featureLayout);

        for(int i=0; i < featureLayout.getChildCount(); i++){

            ImageButton button = (ImageButton) featureLayout.getChildAt(i);
            button.getBackground().setColorFilter(android.R.drawable.btn_default, PorterDuff.Mode.SRC_ATOP);
        }

    }

    private void listenForClicksErase(final Dialog brushDialog) {
        ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                drawView.setBrushSize(smallBrush);
                brushDialog.dismiss();
            }
        });
        ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                drawView.setBrushSize(mediumBrush);
                brushDialog.dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                drawView.setBrushSize(largeBrush);
                brushDialog.dismiss();
            }
        });
    }


    private void listenForClicksSizes(final Dialog brushDialog){
        ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setBrushSize(smallBrush);
                drawView.setLastBrushSize(smallBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            }
        });

        ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setBrushSize(mediumBrush);
                drawView.setLastBrushSize(mediumBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            }
        });

        ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setBrushSize(largeBrush);
                drawView.setLastBrushSize(largeBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            }
        });
    }
}
