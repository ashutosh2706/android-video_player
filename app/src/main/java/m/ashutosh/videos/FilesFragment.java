package m.ashutosh.videos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import static m.ashutosh.videos.MainActivity.videoFiles;

public class FilesFragment extends Fragment {

    RecyclerView recyclerView;
    View view;
    VideoAdapter videoAdapter;
    public FilesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_files, container, false);
        recyclerView = view.findViewById(R.id.recycler_files);
        if(videoFiles != null && videoFiles.size() > 0){
            videoAdapter = new VideoAdapter(getContext(),videoFiles);
            recyclerView.setAdapter(videoAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        }else {
            Snackbar.make(recyclerView,"No Videos Found",Snackbar.LENGTH_LONG).show();
        }
        return view;
    }
}
