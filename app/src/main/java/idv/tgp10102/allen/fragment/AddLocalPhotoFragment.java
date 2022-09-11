package idv.tgp10102.allen.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.io.File;
import java.io.IOException;
import java.util.Objects;

import idv.tgp10102.allen.R;

public class AddLocalPhotoFragment extends Fragment {
    private static final String TAG = "Tag AddLocalPhotoFragment";
    private Activity activity;
    private ImageView ivAddPhoto_Local;
    private String photoPath;

    public AddLocalPhotoFragment(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_local_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        findViews(view);
        updatePhotosView();
    }

    private void updatePhotosView() {
        if (Objects.equals(this.photoPath, null)) {
            return;
        }

        ivAddPhoto_Local.setImageResource(R.drawable.baseline_add_black_48);
        File filePicPath = new File(this.photoPath.toString());
        Bitmap bitmap = null;
        //設定預設圖片尺寸
        if(Objects.equals(photoPath,"-1")){
            ViewGroup.LayoutParams params = ivAddPhoto_Local.getLayoutParams();
            params.height = 300;
            params.width = 300;
            ivAddPhoto_Local.setLayoutParams(params);
        }

        try {
            ImageDecoder.Source source = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                source = ImageDecoder.createSource(filePicPath);
                bitmap = ImageDecoder.decodeBitmap(source);
                ivAddPhoto_Local.setImageBitmap(bitmap);
            }else{
                bitmap = BitmapFactory.decodeFile(filePicPath.toString());
                ivAddPhoto_Local.setImageBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findViews(View view) {
        ivAddPhoto_Local = view.findViewById(R.id.ivAddPhoto_Local);
    }
}