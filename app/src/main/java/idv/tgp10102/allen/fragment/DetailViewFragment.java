package idv.tgp10102.allen.fragment;

import static idv.tgp10102.allen.MainActivity.REQUEST_P1;
import static idv.tgp10102.allen.MainActivity.REQUEST_P2;
import static idv.tgp10102.allen.MainActivity.REQUEST_P3;
import static idv.tgp10102.allen.MainActivity.REQUEST_P4;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.R;

public class DetailViewFragment extends Fragment {
    private static final String TAG = "Tag DetailViewFragment";
    private Activity activity;
    private TextView tvMessage;
    private Spinner spinner;
    private TextView textSpinner;
    private ImageView imageView1,imageView2,imageView3,imageView4;
    private ConstraintLayout constraintLayout;
    private ImageView conLayIvBigPic;
    private List<String> memberList;

    private File dirMember;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        memberList = new ArrayList<>();
        findViews(view);
        load();
        handleImageViewToBigPic();
        handleBigPicToImagerView();

    }


    @Override
    public void onResume() {
        super.onResume();
         hadleSpinner();
    }

    private void findViews(View view) {
        spinner = view.findViewById(R.id.spinner_detail);
        tvMessage = view.findViewById(R.id.tvMessage_detail);
        imageView1 = view.findViewById(R.id.iv1_detail);
        imageView2 = view.findViewById(R.id.iv2_detail);
        imageView3 = view.findViewById(R.id.iv3_detail);
        imageView4 = view.findViewById(R.id.iv4_detail);

        constraintLayout = view.findViewById(R.id.conLayoutBigPic);
        conLayIvBigPic = view.findViewById(R.id.ivBigCrop);
    }

    private void hadleSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,R.layout.item_spinner,memberList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                textSpinner =(TextView) view;
                if(Objects.equals(textSpinner,null)){
                    return;
                }
                updateData(textSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void updateData(TextView textView){

        StringBuilder sbTemp = new StringBuilder(String.valueOf(textView.getText()));
        Member member = ComMethod.loadMember(activity, sbTemp.toString());

        if(member ==null){
            return ;
        }
        //textView.setText(travel.getStringName());
        tvMessage.setText(member.getStringMessage());
        Bitmap bitmap;
        File filePicPath;
        bitmap = null;
        filePicPath = null;

        imageView1.setImageResource(R.drawable.ic_baseline_add_circle_outline);
        imageView2.setImageResource(R.drawable.ic_baseline_add_circle_outline);
        imageView3.setImageResource(R.drawable.ic_baseline_add_circle_outline);
        imageView4.setImageResource(R.drawable.ic_baseline_add_circle_outline);
        try {
            //1
            filePicPath = new File(member.getStringPhotosPath().toString());
            imageView1.setImageBitmap(ComMethod.bitmapToImageFilePath(bitmap, filePicPath));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleBigPicToImagerView() {
        conLayIvBigPic.setOnClickListener(view -> {
            conLayIvBigPic.setVisibility(View.INVISIBLE);
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);
            imageView3.setVisibility(View.VISIBLE);
            imageView4.setVisibility(View.VISIBLE);
        });
    }

    private void handleImageViewToBigPic() {
        imageView1.setOnClickListener(view -> {
            onButtonBigPicClick(view,REQUEST_P1);
        });
        imageView2.setOnClickListener(view -> {
            onButtonBigPicClick(view,REQUEST_P2);
        });
        imageView3.setOnClickListener(view -> {
            onButtonBigPicClick(view,REQUEST_P3);
        });
        imageView4.setOnClickListener(view -> {
            onButtonBigPicClick(view,REQUEST_P4);
        });

    }

    private void onButtonBigPicClick(View view,int code){
        if( Objects.equals(spinner.toString(),null) ){
            return;
        }
        StringBuilder sbTemp = new StringBuilder(String.valueOf(textSpinner.getText()));
        Member member = ComMethod.loadMember( activity, sbTemp.toString() );

        if(member ==null){
            return ;
        }
        conLayIvBigPic.setVisibility(View.VISIBLE);

        imageView1.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);

        tvMessage.setText(member.getStringMessage());
        Bitmap bitmap;
        File filePicPath;
        bitmap = null;
        filePicPath = null;

        try {
            switch (code){
                case(REQUEST_P1):
                    filePicPath = new File(member.getStringPhotosPath().toString());
                    conLayIvBigPic.setImageBitmap(ComMethod.bitmapToImageFilePath(bitmap, filePicPath));
                    break;
                case(REQUEST_P2):
                    break;
                case(REQUEST_P3):
                    break;
                case(REQUEST_P4):
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        conLayIvBigPic.setVisibility(View.INVISIBLE);

        dirMember = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File f = new File(dirMember.toString());
        File[] files= f.listFiles();

        StringBuilder s;
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                memberList.add(String.valueOf(new StringBuilder(files[i].getName().trim())));
            }

        }
    }

}