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
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class EditFragment extends Fragment {
    private static final String TAG = "Tag EditFragment";
    private Activity activity;
    private AutoCompleteTextView etName;
    private EditText etMessage;
    private ImageButton ibSave,ibLoad,ibDelete,ibUploadCould,ibCreateNew;
    private ImageButton ibToClose,ibToOpen,ibAddPhoto,ibTakePic;
    private ArrayAdapter<String> adapter;
    private MyViewPager2Adapter myViewPager2Adapter;

    private Integer pagerNumber;

    private File dir;
    private File dirMember;
    private File file;
    private File fileDest;
    private Uri srcUri;
    private int handleRequestCode = 0;
    private ViewPager2 viewPager2;
    private TextView tvSysMessage;
    private CardView cvButtonBarToOpen,cvButtonBarToClose;
    public static List<String> photosSavedList;
    public static List<String> currentEditList;

    public final static String EMPTY = "-1";

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
        photosSavedList = new ArrayList<>();
        findViews(view);
        ComMethod.getMemberList(activity);
        handleInitialAndVisibility();

        contentResolver = activity.getContentResolver();
        takePicLauncher = getLauncher();
        cropPicLauncher = getCropPicLauncher();

        handleAutoCompleteTextView();
        handleButton();


//        ibUploadCould.setOnClickListener(v -> {
//            tvPageNumber.setText(""+(viewPager2.getCurrentItem()+1));
//
//        });

    }

    //1:createNew  2:增加 1 photoPosition 3:LoadSave檔 4:拍照完更新
    private void upDateCurrentEditList(int requestCode,String s, Integer position) {
        switch (requestCode){
            case REQUEST_P1:
                currentEditList = new ArrayList<>();
                currentEditList.add(EMPTY);
                break;
            case REQUEST_P2:
                currentEditList.add(EMPTY);
                break;
            case REQUEST_P3:
                currentEditList = photosSavedList;
                break;
            case REQUEST_P4:
                currentEditList.set(position,s);
                break;
        }
    }

    private void handleButton() {
        ibCreateNew.setOnClickListener(v ->{
            upDateCurrentEditList(REQUEST_P1,null,0);
            etName.setText("");
            etMessage.setText("");
//            myViewPager2Adapter = new MyViewPager2Adapter((FragmentActivity) activity,currentEditList);
            viewPager2.setAdapter(myViewPager2Adapter);
        });
        ibAddPhoto.setOnClickListener(v -> {
            upDateCurrentEditList(REQUEST_P2,null,null);
            viewPager2.setAdapter(myViewPager2Adapter);
            viewPager2.setCurrentItem(viewPager2.getAdapter().getItemCount());
        });

        handleBtload();
        handleBtTakePic();
        handleBtSave();
        handleBtDelete();

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

        String sName = etName.getText().toString().trim();
        if( Objects.equals(sName,null) ){
            tvSysMessage.setText(R.string.name_null);
            return;
        }
        StringBuilder sbTemp = new StringBuilder(sName);
        String sDebug = sName.toString();
        Member member = ComMethod.loadMember(activity,sbTemp.toString());

        if(member ==null){
            tvSysMessage.setText(R.string.file_nodata);
            return ;
        }
        dirMember = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File f = new File(dirMember.toString() + "/" + sName );
        File[] files= f.listFiles();
        StringBuilder s;
        currentEditList = new ArrayList<>();
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                photosSavedList.add(String.valueOf(new StringBuilder( files[i].getName() )));

                currentEditList.add(f.toString() +"/"+photosSavedList.get(i).toString());
            }
            if(!Objects.equals(photosSavedList,null)  && ( photosSavedList.size() > 0) ){
//                upDateCurrentEditList(REQUEST_P3,null,null);
                viewPager2.setAdapter(myViewPager2Adapter);
            }
        }


        etName.setText(member.getStringName());
        etMessage.setText(member.getStringMessage());
        UpdatePhotosSavedList(member);

//        viewPager2.setAdapter(new MyViewPager2Adapter((FragmentActivity) activity,photosList));
//        imageView1.setImageResource(R.drawable.ic_baseline_add_circle_outline);

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
                currentEditList = null;
                viewPager2.setAdapter(myViewPager2Adapter);
                handleAutoCompleteTextView();
            }
        });
    }

    private void save(){
        //檢查是否已有此Name member
        StringBuilder sbTemp = new StringBuilder(String.valueOf(etName.getText()));
        if( sbTemp.equals(null) ) {
            tvSysMessage.setText(R.string.name_null);
            return;
        }
        ComMethod.getMemberList(activity);

        File fileTemp = new File(sbTemp.toString());
        Member mCheck = ComMethod.loadMember(activity,fileTemp.toString());

        if(mCheck == null){
            tvSysMessage.setText(R.string.create_new);
        }else{
            tvSysMessage.setText(R.string.modify_file);
        }
        if( Objects.equals(currentEditList,null)
                || (currentEditList.size()<=0)
                || (Objects.equals(currentEditList.get(0), EMPTY)) ) {
            tvSysMessage.setText(R.string.file_nodata);
            return;
        }

        try(
                //Object儲存資料夾
                FileOutputStream fos = activity.openFileOutput(sbTemp.toString(),MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        )
        {
            Member member = new Member();
            StringBuilder sbName = new StringBuilder(String.valueOf(etName.getText()).trim());
            StringBuilder sbMessage = new StringBuilder(String.valueOf(etMessage.getText()).trim());
            dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            //建立資料夾
            dirMember = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS + "/"+sbTemp.toString());

            File fileCrop = null ;
            File fileOrigin = null ;
            StringBuilder sbFile = null;
            int count = 0;
            file = new File(dirMember,sbTemp.toString());

            for(int i=0; i < currentEditList.size(); i++){
                if( Objects.equals(currentEditList.get(0), EMPTY) ) {
                    break;
                }

                fileCrop =new File(dir,""+takePicCrop+ "_" + count +".jpg");
                fileOrigin =new File(dir,""+takePicOrigin+ "_" + count +".jpg");
                fileDest = new File(dirMember,""+System.currentTimeMillis()+ count++ +".jpg");
                //先考慮裁切檔案
                if( currentEditList.get(i).contains("Crop") ){
                    copyPicture(fileCrop, new StringBuilder(fileDest.toString()));
                }

                fileCrop.delete();
                fileOrigin.delete();
            }


            sbFile = new StringBuilder(file.toString());

            member.setStringPhotosPath(sbFile);


            member.setStringName(sbName);
            member.setStringMessage(sbMessage);
            oos.writeObject(member);

            etName.setText(null);
            etMessage.setText(null);

            tvSysMessage.setText(R.string.save);

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

                        File fliePathTemp =new File(dir,""+takePicCrop+ "_" + pagerNumber +".jpg");
                        upDateCurrentEditList(REQUEST_P4,fliePathTemp.toString(),pagerNumber);

                        viewPager2.setAdapter(myViewPager2Adapter);
                        viewPager2.setCurrentItem(viewPager2.getAdapter().getItemCount());

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
        tvSysMessage = view.findViewById(R.id.tvSysMessage);
        etMessage = view.findViewById(R.id.etMessage);
        ibSave = view.findViewById(R.id.ibSaveMember);
        ibLoad = view.findViewById(R.id.ibLoad);
        ibDelete = view.findViewById(R.id.ibDeleteMember);
        ibUploadCould = view.findViewById(R.id.ibUploadCloud);
        ibCreateNew = view.findViewById(R.id.ibCreateNewMember);

        ibAddPhoto = view.findViewById(R.id.ibAdd_photo);
        ibTakePic = view.findViewById(R.id.ibCamera_photo);

        viewPager2 = view.findViewById(R.id.viewPager2);
        cvButtonBarToOpen = view.findViewById(R.id.cvPhotoButtonOpenBar);
        cvButtonBarToClose = view.findViewById(R.id.cvPhotoButtonCloseBar);
        ibToClose = view.findViewById(R.id.ibBarOpenToclose);
        ibToOpen = view.findViewById(R.id.ibBarCloseToOpen);

    }

    private void handleInitialAndVisibility() {
        pagerNumber = 0;
        myViewPager2Adapter = new MyViewPager2Adapter((FragmentActivity) activity,currentEditList);
        viewPager2.setAdapter(myViewPager2Adapter);

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
       pagerNumber = viewPager2.getCurrentItem()+1;
        ibTakePic.setOnClickListener(v -> {
            pagerNumber = viewPager2.getCurrentItem();
            onButtonTakeClick(pagerNumber);
        });

    }

    public void onButtonTakeClick(int takePicCodePos){
        handleRequestCode = takePicCodePos;
        file = createFile(takePicOrigin);
        srcUri = FileProvider.getUriForFile(activity,
                activity.getPackageName()+".fileProvider",file);

        takePicLauncher.launch(srcUri);
    }

    private void UpdatePhotosSavedList (Member member) {
        photosSavedList = new ArrayList<>();

//        if(!Objects.equals(member.getStringPhotosPath().toString(),null) ){
//            photosList.add(member.getStringPhotosPath().toString());
//        }
//
//        if(photosList.isEmpty()){
//            photosList.add("-1");
//        }

    }

    class MyViewPager2Adapter extends FragmentStateAdapter {
        private List<String> list;

        public MyViewPager2Adapter(@NonNull FragmentActivity fragmentActivity,List<String> list) {
            super(fragmentActivity);
            this.list = list;
        }

        public void setMyViewPager2Adapter(List<String> list) {
            this.list = list;
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
           StringBuilder s = new StringBuilder(currentEditList.get(position));
           cvButtonBarToOpen.setVisibility(View.VISIBLE);
           cvButtonBarToClose.setVisibility(View.INVISIBLE);
           return new AddPhotosFragment(s);
        }

        @Override
        public int getItemCount() {
            return currentEditList == null ? 0 : currentEditList.size();
        }
    }

}