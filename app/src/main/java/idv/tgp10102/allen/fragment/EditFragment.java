package idv.tgp10102.allen.fragment;

import static idv.tgp10102.allen.MainActivity.*;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.Travel;

public class EditFragment extends Fragment {
    private static final String TAG = "Tag EditFragment";
    private Activity activity;
    private AutoCompleteTextView etName;
    private EditText etMessage;
    private ImageButton ibSave,ibLoad,ibDelete,ibShare,ibToClose,ibToOpen;
    private ArrayAdapter<String> adapter;

    private File dir;
    private File dirMember;
    private File file;
    private File fileDest;
    private Uri srcUri;
    private int handleRequestCode = 0;
    private ViewPager2 viewPager2;
    private TextView tvPageNumber;
    private CardView cvButtonBarToOpen,cvButtonBarToClose;
    public static List<String> photosList;
    private ContentResolver contentResolver;

    private ActivityResultLauncher<Uri> takePicLauncher;
    private ActivityResultLauncher<Intent> cropPicLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        photosList = new ArrayList<>();

        findViews(view);
        ComMethod.getMemberList(activity);
        handleInitialAndVisibility();

        contentResolver = activity.getContentResolver();
        takePicLauncher = getLauncher();
        cropPicLauncher = getCropPicLauncher();

        handleAutoCompleteTextView();

        handleBtload();
        handleBtTakePic();
        handleBtSave();
        handleBtDelete();


        ibShare.setOnClickListener(v -> {
            tvPageNumber.setText(""+(viewPager2.getCurrentItem()+1));

        });

    }


    private void createNewViewPager2() {
        viewPager2.setAdapter(new MyViewPager2Adapter((FragmentActivity) activity,photosList));
    }

    private void handleAutoCompleteTextView() {
            ComMethod.getMemberList(activity);
            List<String> listTemp = new ArrayList<>();
            for(String temp: ComMethod.memberList){
                listTemp.add(temp.toString());
            }
            adapter = new ArrayAdapter<>(activity,R.layout.name_view,listTemp);
            etName.setAdapter(adapter);
    }

    private void delete(){

        int position = -1;
        boolean flag = false;
        String deleteName = null;
        if(Objects.equals(etName.toString(),null)){
            Toast.makeText(activity, "FileName is null.", Toast.LENGTH_SHORT).show();
            return;
        }
        File dirMember = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        ComMethod.getMemberList(activity);
        if(ComMethod.memberList.size()>0){
            for (int i = 0; i < ComMethod.memberList.size(); i++) {
                if(Objects.equals(etName.getText().toString(),ComMethod.memberList.get(i).toString())){
                    position = i;
                    flag = true;
                    deleteName = etName.getText().toString();
                    break;
                }
            }
            if(!flag){
                Toast.makeText(activity, "No Find File ", Toast.LENGTH_SHORT).show();
            }
        }
        //有該member資料
        if ((position >= 0) && flag){
            file =new File(dirMember,""+deleteName);
            etMessage.setText(file.toString());
            File[] subFileList = file.listFiles();
            if(subFileList != null){
                for(File temp : subFileList) {
                    temp.delete();
                }
            }
            //再刪除本資料
            if(file.delete()){
                Toast.makeText(activity, "Delete Successful", Toast.LENGTH_SHORT).show();
                    File fileObjectPath = new File(activity.getFilesDir(), deleteName);
                    fileObjectPath.delete();
                etName.setText("");
                etMessage.setText("");
            }
        }
    }

    private void handleBtDelete() {
        ibDelete.setOnClickListener(view -> {
            delete();
            handleAutoCompleteTextView();
        });

    }

    private void handleBtload() {
        ibLoad.setOnClickListener(view -> {
            load();
        });
    }

    private void load() {

        if( Objects.equals(etName.toString(),null) ){
            return;
        }
        StringBuilder sbTemp = new StringBuilder(String.valueOf(etName.getText()));
        Travel travel = ComMethod.loadTravel(activity,sbTemp.toString());

        if(travel ==null){
            return ;
        }
        etName.setText(travel.getStringName());
        etMessage.setText(travel.getStringMessage());
        UpdatePhotosList(travel);
        viewPager2.setAdapter(new MyViewPager2Adapter((FragmentActivity) activity,photosList));
//        imageView1.setImageResource(R.drawable.ic_baseline_add_circle_outline);
//        imageView2.setImageResource(R.drawable.ic_baseline_add_circle_outline);
//        imageView3.setImageResource(R.drawable.ic_baseline_add_circle_outline);
//        imageView4.setImageResource(R.drawable.ic_baseline_add_circle_outline);

        Bitmap bitmap = null;
        File filePicPath = null;

//        try {
//
//            //1
//            filePicPath = new File(travel.getStringFilePath1().toString());
//            imageView1.setImageBitmap(bitmapToImageFilePath(bitmap, filePicPath));
//            //2
//            filePicPath = new File(travel.getStringFilePath2().toString());
//            imageView2.setImageBitmap(bitmapToImageFilePath(bitmap, filePicPath));
//            //3
//            filePicPath = new File(travel.getStringFilePath3().toString());
//            imageView3.setImageBitmap(bitmapToImageFilePath(bitmap, filePicPath));
//            //4
//            filePicPath = new File(travel.getStringFilePath4().toString());
//            imageView4.setImageBitmap(bitmapToImageFilePath(bitmap, filePicPath));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    private void handleBtSave() {

        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
                handleAutoCompleteTextView();
            }
        });
    }

    private void save(){
        //檢查是否已有此Name member
        StringBuilder sbTemp = new StringBuilder(String.valueOf(etName.getText()));
        if( sbTemp.equals(null) ) {
            Toast.makeText(activity, "Name null...", Toast.LENGTH_SHORT).show();
            return;
        }
        ComMethod.getMemberList(activity);

        File fileTemp = new File(sbTemp.toString());
        Travel tCheck = ComMethod.loadTravel(activity,fileTemp.toString());

        if(tCheck == null){
            Toast.makeText(activity, "CreateNew...", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(activity, "Modify...", Toast.LENGTH_SHORT).show();
        }

        try(
                //Object儲存資料夾
                FileOutputStream fos = activity.openFileOutput(sbTemp.toString(),MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        )
        {
            Travel travel = new Travel();
            StringBuilder sbName = new StringBuilder(String.valueOf(etName.getText()));
            StringBuilder sbMessage = new StringBuilder(String.valueOf(etMessage.getText()));
            dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            dirMember = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS + "/"+sbTemp.toString());
            //建立資料夾

            File file = null ;
            StringBuilder sbFile = null;
            //1
            file =new File(dir,""+takePicCrop+ "_" + REQUEST_P1 +".jpg");
            fileDest = new File(dirMember,sbTemp.toString()+"_"+REQUEST_P1+".jpg");
            copyPicture(file, new StringBuilder(fileDest.toString()));
            file.delete();
            sbFile = new StringBuilder(fileDest.toString());

            travel.setStringFilePath1(sbFile);
            //2
            file =new File(dir,""+takePicCrop+ "_" + REQUEST_P2 +".jpg");
            fileDest = new File(dirMember,sbTemp.toString()+"_"+REQUEST_P2+".jpg");
            copyPicture(file, new StringBuilder(fileDest.toString()));
            file.delete();
            sbFile = new StringBuilder(fileDest.toString());
            travel.setStringFilePath2(sbFile);
            //3
            file =new File(dir,""+takePicCrop+ "_" + REQUEST_P3 +".jpg");
            fileDest = new File(dirMember,sbTemp.toString()+"_"+REQUEST_P3+".jpg");
            copyPicture(file, new StringBuilder(fileDest.toString()));
            sbFile = new StringBuilder(fileDest.toString());
            file.delete();
            travel.setStringFilePath3(sbFile);
            //4
            file =new File(dir,""+takePicCrop+ "_" + REQUEST_P4 +".jpg");
            fileDest = new File(dirMember,sbTemp.toString()+"_"+REQUEST_P4+".jpg");
            copyPicture(file, new StringBuilder(fileDest.toString()));
            sbFile = new StringBuilder(fileDest.toString());
            file.delete();
            travel.setStringFilePath4(sbFile);

            travel.setStringName(sbName);
            travel.setStringMessage(sbMessage);
            oos.writeObject(travel);

            etName.setText(null);
            etMessage.setText(null);

            Toast.makeText(activity, "Save.....", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Log.e(TAG,e.toString());
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }
    }

    private void copyPicture(File file,StringBuilder sbDest) {
        if( !file.exists() ){
            return;
        }
        try(
                FileInputStream fis = new FileInputStream(String.valueOf(file.toString()));
                BufferedInputStream bis = new BufferedInputStream(fis);
                FileOutputStream fos = new FileOutputStream(String.valueOf(sbDest));
                BufferedOutputStream bos = new BufferedOutputStream(fos);

        ) {
            byte[] b = new byte[bis.available()];
            bis.read(b);
            bos.write(b);
            bos.flush();

        }catch (Exception e){
            Log.e(TAG,e.toString());
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }

        }



//    public static Bitmap bitmapToImageFilePath(Bitmap bitmap,File filepath) throws IOException{
//        ImageDecoder.Source source = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//            source = ImageDecoder.createSource(filepath);
//            bitmap = ImageDecoder.decodeBitmap(source);
//        }else{
//            bitmap = BitmapFactory.decodeFile(filepath.toString());
//        }
//        return bitmap;
//    }

//    public void switchSelectBitmap(Bitmap bitmap,int code){
//        switch (code){
//            case (REQUEST_P1):
//                imageView1.setImageBitmap(bitmap);
//                break;
//            case (REQUEST_P2):
//                imageView2.setImageBitmap(bitmap);
//                break;
//            case (REQUEST_P3):
//                imageView3.setImageBitmap(bitmap);
//                break;
//            case (REQUEST_P4):
//                imageView4.setImageBitmap(bitmap);
//                break;
//            default:
//                break;
//        }
//    }

    private  ActivityResultLauncher<Uri> getLauncher(){
        return registerForActivityResult(new ActivityResultContracts.TakePicture(),
                isOk ->{
                    if(isOk){
                        crop();
                    }
                }
        );
    }

    private ActivityResultLauncher<Intent> getCropPicLauncher() {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                ActivityResult -> {
                    if(ActivityResult == null || ActivityResult.getResultCode() != RESULT_OK){
                        return;
                    }
                    try {
                        //取得裁切後圖
                        Intent intent = ActivityResult.getData();
                        if(intent == null){
                            return;
                        }
                        Uri resultUri = UCrop.getOutput(intent);
                        Bitmap bitmap = null ;

                        ImageDecoder.Source source = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            source = ImageDecoder.createSource(contentResolver,resultUri);
                            bitmap = ImageDecoder.decodeBitmap(source);
                        }else{
                            bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(resultUri));
                        }
//                        switchSelectBitmap(bitmap,handleRequestCode);

                    }catch (IOException e){
                        Log.e(TAG,e.toString());
                    }
                }
        );
    }

    private void crop() {
        final Uri dstUri = Uri.fromFile(createFile(takePicCrop));
        UCrop uCrop = UCrop.of(srcUri,dstUri);
        Intent intent = uCrop.getIntent(activity);
        cropPicLauncher.launch(intent);
    }

    private  File createFile(String fileName) {
        dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(dir,"" + fileName + "_" + handleRequestCode +".jpg");
        return  file;
    }

    private void findViews(View view) {

        etName = view.findViewById(R.id.etName);
        etMessage = view.findViewById(R.id.etMessage);
        ibSave = view.findViewById(R.id.ibSave);
        ibLoad = view.findViewById(R.id.ibLoad);
        ibDelete = view.findViewById(R.id.ibDelete);
        ibShare = view.findViewById(R.id.ibShare);

        viewPager2 = view.findViewById(R.id.viewPager2);
        tvPageNumber = view.findViewById(R.id.tvPageNumber_edit);
        cvButtonBarToOpen = view.findViewById(R.id.cvPhotoButtonOpenBar);
        cvButtonBarToClose = view.findViewById(R.id.cvPhotoButtonCloseBar);
        ibToClose = view.findViewById(R.id.ibBarOpenToclose);
        ibToOpen = view.findViewById(R.id.ibBarCloseToOpen);

    }

    private void handleInitialAndVisibility() {
        cvButtonBarToOpen.setVisibility(View.INVISIBLE);
        cvButtonBarToClose.setVisibility(View.VISIBLE);

        ibToOpen.setOnClickListener(view->{
            cvButtonBarToOpen.setVisibility(View.VISIBLE);
            cvButtonBarToClose.setVisibility(View.INVISIBLE);
        });
        ibToClose.setOnClickListener(view ->{
            cvButtonBarToOpen.setVisibility(View.INVISIBLE);
            cvButtonBarToClose.setVisibility(View.VISIBLE);
        });

    }

    private void handleBtTakePic() {

//        imageView1.setOnClickListener(view -> {
//            onButtonTakeClick(REQUEST_P1);
//        });
//
//        imageView2.setOnClickListener(view -> {
//            onButtonTakeClick(REQUEST_P2);
//        });
//
//        imageView3.setOnClickListener(view -> {
//            onButtonTakeClick(REQUEST_P3);
//        });
//        imageView4.setOnClickListener(view -> {
//            onButtonTakeClick(REQUEST_P4);
//        });

    }

    public void onButtonTakeClick(int takePicCodePos){
        handleRequestCode = takePicCodePos;
        file = createFile(takePicOrigin);
        srcUri = FileProvider.getUriForFile(activity,
                activity.getPackageName()+".fileProvider",file);

        takePicLauncher.launch(srcUri);
    }

    private void UpdatePhotosList (Travel travel) {
        photosList = new ArrayList<>();
        if( !Objects.equals(travel.getStringFilePath1().toString(),null) ){
            photosList.add(travel.getStringFilePath1().toString());
        }
        if( !Objects.equals(travel.getStringFilePath2().toString(),null) ){
            photosList.add(travel.getStringFilePath2().toString());
        }
        if( !Objects.equals(travel.getStringFilePath3().toString(),null) ){
            photosList.add(travel.getStringFilePath3().toString());
        }
        if( !Objects.equals(travel.getStringFilePath4().toString(),null) ){
            photosList.add(travel.getStringFilePath4().toString());
        }

        if(photosList.isEmpty()){
            photosList.add("-1");
        }

    }

    class MyViewPager2Adapter extends FragmentStateAdapter {
        private List<String> list;

        public MyViewPager2Adapter(@NonNull FragmentActivity fragmentActivity,List<String> list) {
            super(fragmentActivity);
            this.list = list;
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
           StringBuilder s = new StringBuilder(photosList.get(position));
           cvButtonBarToOpen.setVisibility(View.VISIBLE);
           cvButtonBarToClose.setVisibility(View.INVISIBLE);
           return new AddPhotosFragment(s);
        }

        @Override
        public int getItemCount() {
            return photosList == null ? 0 : photosList.size();
        }
    }

}