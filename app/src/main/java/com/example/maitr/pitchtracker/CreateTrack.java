package com.example.maitr.pitchtracker;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class CreateTrack extends AppCompatActivity {
    StringBuilder writer,out;
    float ptc;
    long startTime, curTime;
    boolean running;
    TextView tv;
    AlertDialog.Builder builder;
    private DecimalFormat dcf = new DecimalFormat("#.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_track);
        writer = new StringBuilder();
        running = false;
        curTime=startTime=0;
        tv = findViewById(R.id.textView);

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100,1024,0);
        final GetPitch gp = new GetPitch();

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    float fp = 0.0f;
                    @Override
                    public void run() {
                        if(pitchInHz > 25 && pitchInHz < 1500){
                            ptc = pitchInHz;
                            fp = gp.getDev(ptc);
                            if(fp>0.0f) {
                                tv.setText(gp.get(ptc)+"\n"+dcf.format(fp)+"% \u2191");
                            } else{
                                tv.setText(gp.get(ptc)+"\n"+dcf.format(fp*-1)+"% \u2193");
                            }
                        } else if(pitchInHz<0){
                            ptc=0;
                            tv.setText("");
                        }
                    }
                });
            }
        }; //U+2191

        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();

        Timer tmr = new Timer();
        tmr.scheduleAtFixedRate(new TimerTask() {
            int t=0;
            String pP = "",curP;
            @Override
            public void run() {
                if(running) {
                    if (ptc > 0) {
                          curP = gp.get(ptc);
                        if (pP.equals(curP)) t++;
                        else {
                            t = 0;
                            pP = curP;
                        }
                        if (t >= 10) {
                            addToOutput(curTime-50, curP);
                            t = 0;
                            pP = curP;
                        }
                    }
                    curTime+=5;
                }
            }
        },0,5);

        builder = new AlertDialog.Builder(this);
        builder.setMessage("Enter a title for this piece");
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.sample_save_view, null);
        final EditText edt = dialogView.findViewById(R.id.username), edt2 = dialogView.findViewById(R.id.password);
        builder.setView(dialogView);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                save(edt.getText().toString(), edt2.getText().toString());
            }
        });

        final FloatingActionButton fb = findViewById(R.id.floatingActionButton);
        FloatingActionButton fb2 = findViewById(R.id.floatingActionButton2);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(running){
                    running = false;
                    fb.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                } else {
                    fb.setImageResource(R.drawable.ic_pause_black_24dp);
                    running=true;
                }
            }
        });
        fb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curTime>0){
                    running=false;
                    AlertDialog b = builder.create();
                    b.setCancelable(false);
                    b.setCanceledOnTouchOutside(false);
                    b.show();
                } else {
                    Toast.makeText(getApplicationContext(),"Recording not started",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void save(String title, String desc){
        out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        out.append("<file><title>"+title+"</title>");
        out.append("<desc>"+desc+"</desc>");
        out.append("<length>"+curTime+"</length><notes>");
        out.append(writer.toString());
        out.append("</notes></file>");
        File f0 = new File(getFilesDir(),"trks");
        if(!f0.exists()) f0.mkdir();
        File file = new File(f0.getAbsolutePath(), "rcrd"+getIntent().getIntExtra("FILE_NO",0)+".xml");
        try {
            if(!file.exists()) file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(out.toString().getBytes());
            fout.close();
            finish();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    private void addToOutput(long time, String p){
        writer.append("<note time=\""+time+"\">"+p+"</note>");
    }

}
