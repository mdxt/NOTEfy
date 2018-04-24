package com.example.maitr.pitchtracker;

public class GetPitch {
    private float[] oct;
    private String[] notes = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
    private int[] E = {28, 33, 38, 43, 47, 52};
    private int[] dC = {24, 31, 36, 41, 45, 50};

    GetPitch(){
        oct = new float[]{16.35f,17.32f,18.35f,19.45f,20.6f,21.83f,23.12f,24.5f,25.96f,27.5f,29.14f,30.87f,
                          32.7f,34.65f,36.71f,38.89f,41.2f,43.65f,46.25f,49.0f,51.91f,55.0f,58.27f,61.74f,
                          65.41f,69.3f,73.42f,77.78f,82.41f,87.31f,92.5f,98.0f,103.83f,110.0f,116.54f,123.47f,
                          130.81f,138.59f,146.83f,155.56f,164.81f,174.61f,185.0f,196.0f,207.65f,220.0f,233.08f,246.94f,
                          261.63f,277.18f,293.66f,311.13f,329.63f,349.23f,369.99f,392.0f,415.3f,440.0f,466.16f,493.88f,
                          523.25f,554.37f,587.33f,622.25f,659.25f,698.46f,739.99f,783.99f,830.61f,880.0f,923.33f,987.77f,
                          1046.5f,1108.73f,1174.66f,1244.51f,1318.51f,1396.91f,1479.98f,1567.98f,1661.22f,1760.0f,1864.66f,1975.53f};
    }

    public float[] getFreqsFor(String i){
        switch(i){
            case "E": return new float[]{oct[28], oct[33], oct[38], oct[43], oct[47], oct[52]};
            case "dC": return new float[]{oct[24], oct[31], oct[36], oct[41], oct[45], oct[50]};
        }
        return null;
    }

    public int[] getPositions(String n, String t){
        int[] out = new int[6], tun = new int[6];
        int k = loc(n);
        switch(t){
            case "E": tun = E; break;
            case "dC": tun = dC; break;
        }
        for(int i=0; i<6; i++){
            if(k-tun[i]>=0 && k-tun[i]<=24) out[i] = k-tun[i];
            else out[i] = -1;
        }
        return out;
    }

    private int search(float val, int st, int en){
        int mid = (en+st)/2;
        if(mid==st || oct[mid] == val){
            return mid;
        }
        else if(oct[mid] < val){
            return search(val,mid, en);
        }
        else{
            return search(val,st, mid);
        }
    }

    public String get(Float f){
        int j = search(f, 0 ,oct.length-1);
        if(Math.abs(oct[j]-f) <= Math.abs(oct[j+1]-f)){
            return notes[j%12]+""+(j/12);
        } else {
            return notes[(j+1)%12]+""+(j+1)/12;
        }
    }

    public float getDev(Float f){
        int j = search(f, 0 ,oct.length-1);
        if(Math.abs(oct[j]-f) <= Math.abs(oct[j+1]-f)){
            return (oct[j]-f)/(oct[j]*0.01f);
        } else {
            return (oct[j+1]-f)/(oct[j+1]*0.01f);
        }
    }

    private int loc(String a) {
        int i = -1;
        i = 12*Character.getNumericValue(a.charAt(a.length()-1));
        a = a.substring(0, a.length()-1);
        for(int j=0; j<notes.length; j++){
            if(notes[j].equals(a)) i+= j;
        }
        return i;
    }
}
