package com.ypacm.edu.singasong;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import fftpack.RealDoubleFFT;

public class MainActivity extends AppCompatActivity {

    static final int frequency = 44100;
    static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    static final int BLOCK_SIZE = 256;
    RealDoubleFFT fftTrans;
    Button startStopButton;
    boolean startFlag = false;
    RecordAudioTask recordAudioTask;
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;


    Point point = new Point();
    int width;
    int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display mDisplay = getWindowManager().getDefaultDisplay();

        mDisplay.getSize(point);
        width = point.x;
        height = point.y;

        Log.d("MYSIZE", "" + width);
        Log.d("MYSIZE", "" + height);

        startStopButton =(Button) findViewById(R.id.button);
        fftTrans = new RealDoubleFFT(BLOCK_SIZE);

        imageView =(ImageView) findViewById(R.id.imageView);
        bitmap = Bitmap.createBitmap(width,height/4,Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);
    }

    private class RecordAudioTask extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfig, audioFormat);
                Log.v("bufSize", String.valueOf(bufferSize));
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfig, audioFormat, bufferSize);

                short[] audioBuffer = new short[BLOCK_SIZE];
                double[] toTrans = new double[BLOCK_SIZE];

                audioRecord.startRecording();

                while (startFlag) {
                    int result = audioRecord.read(audioBuffer, 0, BLOCK_SIZE);

                    for (int i = 0; i < BLOCK_SIZE && i < result; i++) {
                        toTrans[i] = (double) audioBuffer[i] / Short.MAX_VALUE;
                    }
                    fftTrans.ft(toTrans);
                    publishProgress(toTrans);
                }
                audioRecord.stop();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... values) {

            canvas.drawColor(Color.BLACK);
            for (int i = 0; i < values[0].length; i++) {
                int x ;
                x = (int) ( width /(i+1));
                int downy = (int) (height/4 - (values[0][i] * 10));
                int upy = height/4;

                canvas.drawLine(x, downy, x, upy, paint);
            }
            imageView.invalidate();
        }
    }

    public void onClick(View v) {
        if(startFlag){
            startStopButton.setText("START");
            recordAudioTask.cancel(true);
            Toast.makeText(this,"it will stop!!!",Toast.LENGTH_LONG).show();
        }
        else{
            startStopButton.setText("STOP");
            recordAudioTask = new RecordAudioTask();
            recordAudioTask.execute();
            Toast.makeText(this,"it will start!!!",Toast.LENGTH_LONG).show();
        }
        startFlag = !startFlag;

    }


}
