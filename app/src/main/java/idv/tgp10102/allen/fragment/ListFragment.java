package idv.tgp10102.allen.fragment;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.Travel;

public class ListFragment extends Fragment {
    private static final String TAG = "Tag_ListFragment";
    private Activity activity;
    private RecyclerView recyclerView;
    private List<String> memberlist;
    private File dirMember;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        memberlist = new ArrayList<>();
        load();
        findViews(view);
        handleRcyclerView();
    }

    private void load() {

        dirMember = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File f = new File(dirMember.toString());
        File[] files= f.listFiles();
        StringBuilder s;
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                memberlist.add(String.valueOf(new StringBuilder(files[i].getName().trim())));
            }
        }
    }

    private void findViews(View view) {
        recyclerView =  view.findViewById(R.id.recyclerView);
    }

    private List<Travel> getTravelList() {

        List<Travel> travelList = new ArrayList<>();
        if(memberlist.size() <= 0){
            return null;
        }
        for (int i = 0; i < memberlist.size(); i++) {

            StringBuilder sbTemp = new StringBuilder(String.valueOf(memberlist.get(i)));
            Travel travel = ComMethod.loadTravel(activity,sbTemp.toString());
            travelList.add(travel);
        }
        return travelList;
    }


    private void handleRcyclerView() {
        recyclerView.setAdapter(new MyAdapter(activity, getTravelList()));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        Context context;
        List<Travel> list;

        public MyAdapter(Context context, List<Travel> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_view, parent,false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Travel travel = list.get(position);

            Bitmap bitmap = null;
            File filePicPath = null;
            try {
                filePicPath = new File(travel.getStringFilePath1().toString());
                holder.ivPic1.setImageBitmap( EditFragment.bitmapToImageFilePath(bitmap,filePicPath) );
                filePicPath = new File(travel.getStringFilePath2().toString());
                holder.ivPic2.setImageBitmap( EditFragment.bitmapToImageFilePath(bitmap,filePicPath) );
                filePicPath = new File(travel.getStringFilePath3().toString());
                holder.ivPic3.setImageBitmap( EditFragment.bitmapToImageFilePath(bitmap,filePicPath) );
                filePicPath = new File(travel.getStringFilePath4().toString());
                holder.ivPic4.setImageBitmap( EditFragment.bitmapToImageFilePath(bitmap,filePicPath) );

            } catch (IOException e) {
                e.printStackTrace();
            }

            holder.tvName.setText(travel.getStringName());
            holder.tvMessage.setText(travel.getStringMessage());

        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName,tvMessage;
            ImageView ivPic1,ivPic2,ivPic3,ivPic4;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName_item);
                tvMessage = itemView.findViewById(R.id.tvMessage_item);
                ivPic1 = itemView.findViewById(R.id.iv1_item);
                ivPic2 = itemView.findViewById(R.id.iv2_item);
                ivPic3 = itemView.findViewById(R.id.iv3_item);
                ivPic4 = itemView.findViewById(R.id.iv4_item);

            }
        }

    }
}