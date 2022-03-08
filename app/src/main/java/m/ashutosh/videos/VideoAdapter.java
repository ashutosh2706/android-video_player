package m.ashutosh.videos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private Context mContext;
    static ArrayList<VideoFiles> videoFiles;
    View view;

    public VideoAdapter(Context mContext, ArrayList<VideoFiles> videoFiles) {
        this.mContext = mContext;
        this.videoFiles = videoFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.video_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.filename.setText(videoFiles.get(position).getTitle());
        int dur = Integer.parseInt(videoFiles.get(position).getDuration());
        String videoDuration = userFriendlyTime(dur);
        holder.duration.setText(videoDuration);
        Glide.with(mContext).load(new File(videoFiles.get(position).getPath())).into(holder.thumb);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,PlayerActivity.class);
                intent.putExtra("position",position);

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView thumb;
        TextView filename,duration;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.thumbnail);
            filename = itemView.findViewById(R.id.video_filename);
            duration = itemView.findViewById(R.id.video_duration);

        }
    }

    private String userFriendlyTime(int duration) {

        String se = String.valueOf(duration/1000);
        int seconds = Integer.parseInt(se);
        String min = String.valueOf(seconds % 60);
        String hour = String.valueOf(seconds / 60);
        String singleDigit = "";
        String doubleDigit = "";

        singleDigit = hour + ":" + "0" + min;
        doubleDigit = hour + ":" + min;

        if(min.length() == 1) {
            return singleDigit;
        }else {
            return doubleDigit;
        }
    }
}
