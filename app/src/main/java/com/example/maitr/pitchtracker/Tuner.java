package com.example.maitr.pitchtracker;


import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
/**
 * A simple {@link Fragment} subclass.
 */
public class Tuner extends Fragment {

    public Tuner() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tuner, container, false);
    }
    private String tuning;
    private float[] freqs;
    private int prev;
    private TextView tv;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private TextView tv5;
    private TextView tv6;
    private DecimalFormat dcf = new DecimalFormat("#.#");
    private boolean intrvl;
    private SharedPreferences.OnSharedPreferenceChangeListener spl;

    private void setDisplay(float ptc){
        int t=0;
        for(int i=1;i<freqs.length;i++){
         if(Math.abs(ptc-freqs[i]) < Math.abs(ptc-freqs[t])){
             t=i;
         }
        }
        float dx = ((freqs[t]-ptc)/freqs[t])*100;
        map.get(prev+1).setBackgroundResource(R.color.white);
        if(Math.abs(dx) < 1.0f){
            map.get(t+1).setBackgroundResource(R.color.green);
            tv.setBackgroundResource(R.drawable.circular_textview_drawable2);
        }
        else{
            map.get(t+1).setBackgroundResource(R.color.holo);
            tv.setBackgroundResource(R.drawable.circular_textview_drawable);
        }
        prev=t;
        if(dx>=0) tv.setText("Tune up!\n"+dcf.format(Math.abs(dx))+"% off.");
        else tv.setText("Tune down!\n"+dcf.format(Math.abs(dx))+"% off.");
    }

    private final HashMap<Integer, TextView> map = new HashMap<>();
    private final StringBuilder sb = new StringBuilder();

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        tv = getActivity().findViewById(R.id.textView);
        tv1 = getActivity().findViewById(R.id.s1);
        tv2 = getActivity().findViewById(R.id.s2);
        tv3 = getActivity().findViewById(R.id.s3);
        tv4 = getActivity().findViewById(R.id.s4);
        tv5 = getActivity().findViewById(R.id.s5);
        tv6 = getActivity().findViewById(R.id.s6);
        tuning = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("tuning","E");
        prev=0;
        intrvl=false;
        map.put(1,tv1);
        map.put(2,tv2);
        map.put(3,tv3);
        map.put(4,tv4);
        map.put(5,tv5);
        map.put(6,tv6);
        final GetPitch gp2 = new GetPitch();
        freqs = gp2.getFreqsFor(tuning);
        for(int i=0; i<freqs.length; i++) map.get(i+1).setText(gp2.get(freqs[i]));
        spl = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                tuning = sharedPreferences.getString(s,"E");
                freqs = gp2.getFreqsFor(tuning);
                for(int i=0; i<freqs.length; i++) map.get(i+1).setText(gp2.get(freqs[i]));
            }
        };

        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).registerOnSharedPreferenceChangeListener(spl);

        Timer tmr = new Timer();
        tmr.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                intrvl=true;
            }
        },0,10);
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                int t=0;
                if(getActivity()!=null && intrvl) getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setDisplay(pitchInHz);
                        intrvl=false;
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher2").start();
    }
}
