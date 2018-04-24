package com.example.maitr.pitchtracker;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Xml;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class TrackDisplay extends AppCompatActivity {
private XmlPullParser parser;
private String path;
private GetPitch gp;
private TextView tv5;

    private String toTime(String time){
        Long t = Long.parseLong(time);
        String mil = ""+(t%1000);
        t/=1000;
        return (t/60)+":"+(t%60)+"."+mil;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_display);
        path = getFilesDir().getAbsolutePath()+"/trks/"+getIntent().getStringExtra("FILE_NAME");
        gp = new GetPitch();
        TextView tv1 = findViewById(R.id.textView2);
        TextView tv2 = findViewById(R.id.textView3);
        TextView tv3 = findViewById(R.id.textView4);
        TextView tv4 = findViewById(R.id.textView5);
        tv5 = findViewById(R.id.textView6);


        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder(),sb3 = new StringBuilder();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());;
        String tuning = sharedpreferences.getString("tuning","E");
        float[] f = gp.getFreqsFor(tuning);
        String[] freqs = new String[6];
        for(int i=0; i<6; i++) freqs[i] = gp.get(f[i]);

        try {
            FileInputStream fin = new FileInputStream(path);
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(fin,null);
            int eventType = parser.getEventType();
            String text="",time="",prev="";

            sb1.append("\n");
            sb2.append("\n");
            sb3.append(Arrays.toString(freqs).replaceAll(","," ")+"\n");

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                if(eventType == XmlPullParser.TEXT){
                    text = parser.getText();
                }
                else if(eventType == XmlPullParser.START_TAG && tagname.equalsIgnoreCase("note")){
                    time = parser.getAttributeValue(0);
                }
                else if(eventType == XmlPullParser.END_TAG){
                    if (tagname.equalsIgnoreCase("note")) {
                        sb1.append(toTime(time));
                        if(!text.equalsIgnoreCase(prev)) {
                            sb2.append(text);
                            sb3.append(Arrays.toString(gp.getPositions(text, tuning)).replaceAll(",","   ").replaceAll("-1","X"));
                            prev=text;
                        } else{
                            sb2.append("|");
                        }
                        sb1.append("\n");
                        sb2.append("\n");
                        sb3.append("\n");
                    }
                    else if(tagname.equalsIgnoreCase("desc")){
                        tv1.setText(text);
                    }
                }
                eventType = parser.next();
            }
            tv2.setText(sb1.toString());
            tv3.setText(sb2.toString());
            tv4.setText(sb3.toString());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                int t=0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(pitchInHz > 25 && pitchInHz < 1500) tv5.setText(gp.get(pitchInHz));
                        else tv5.setText("");
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher3").start();
    }
}
