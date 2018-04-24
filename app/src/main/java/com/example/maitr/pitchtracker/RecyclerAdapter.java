package com.example.maitr.pitchtracker;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by maitr on 14-Mar-18.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private List<String[]> names;
    private Intent intent;
    private Context c;
    private String path;

    public RecyclerAdapter(List<String[]> a, Context context){
        names=a;
        c=context;
        path = path = context.getFilesDir().getAbsolutePath()+"/trks/";
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv1;
        private TextView tv2;
        public View layout;
        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            tv1 = itemView.findViewById(R.id.firstLine);
            tv2 = itemView.findViewById(R.id.secondLine);
        }
    }

    public void remove(int position) {
        File file = new File(path+names.get(position)[0]);
        file.delete();
        names.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tv1.setText(names.get(position)[1]);
        holder.tv2.setText(names.get(position)[2]);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(c, TrackDisplay.class);
                intent.putExtra("FILE_NAME", names.get(position)[0]);
                c.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}
