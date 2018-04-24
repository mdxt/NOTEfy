package com.example.maitr.pitchtracker;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FileList extends Fragment {
    private String[] readFromFile(String fl){
        String[] s = new String[2];
        try {
            FileInputStream fin = new FileInputStream(path+fl);
            parser.setInput(fin,null);
            int eventType = parser.getEventType();
            String text="";
            outerloop:
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                if(eventType == XmlPullParser.TEXT){
                    text = parser.getText();
                } else if(eventType == XmlPullParser.END_TAG){
                    if (tagname.equalsIgnoreCase("title")) {
                        s[0]=text;
                    }else if (tagname.equalsIgnoreCase("length")) {
                        s[1]=toTime(text);
                        break outerloop;
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    String path;
    XmlPullParser parser;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public FileList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        path = getActivity().getFilesDir().getAbsolutePath()+"/trks/";
        mRecyclerView = getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        try {
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        String[] names = new File(path).list(), temp;
        List<String[]> input = new ArrayList<>();
        if(names!=null){
            for(int i=0; i<names.length; i++){
                temp = readFromFile(names[i]);
                input.add(new String[]{names[i], temp[0], temp[1]});
            }}
        mAdapter = new RecyclerAdapter(input, view.getContext());

        ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mAdapter.remove(viewHolder.getAdapterPosition());
                ((MainActivity) getActivity()).itemCount=mAdapter.getItemCount();
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchHelper);
        mRecyclerView.setAdapter(mAdapter);
        touchHelper.attachToRecyclerView(mRecyclerView);
        ((MainActivity) getActivity()).itemCount=mAdapter.getItemCount();
    }

    private String toTime(String time){
        Long t = Long.parseLong(time)/1000;
        return (t/60)+":"+(t%60);
    }
}
