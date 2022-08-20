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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.yalantis.ucrop.UCrop;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class EditFragment extends Fragment {
    private static final String TAG = "Tag EditFragment";
    private Activity activity;
    private AutoCompleteTextView etName;
    private EditText etMessage;
    private ImageButton ibSave,ibLoad,ibDelete,ibUploadCould,ibCreateNew;
    private ImageButton ibAddPhoto,ibTakePic,ibDeletePhoto,ibSelectPhoto,ibBack;
    private CheckBox cbAutoSync;
    private File myDirDocument;
    private ArrayAdapter<String> adapter;
    private List<String> memberListEdit;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private MyViewPager2Adapter myViewPager2Adapter;
    private Integer pagerNumber;
    private File dir;
    private File dirMember;
    private File file;
    private File fileDest;
    private Uri srcUri;
    private int handleRequestCode = 0;
    private ViewPager2 viewPager2;
    private TextView tvEditMessage;
    public List<String> currentEditList;
    public final static String EMPTY = "-1";

    private ContentResolver contentResolver;
    private ActivityResultLauncher<Uri> takePicLauncher;
    private ActivityResultLauncher<Intent> cropPicLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        memberListEdit = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        myDirDocument = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        findViews(view);
        upDateMemberNameList();
        handleInitialAndVisibility();

        contentResolver = activity.getContentResolver();
        takePicLauncher = getLauncher();
        cropPicLauncher = getCropPicLauncher();
        handleAutoCompleteTextView();
        handleButton();

        // 取Bundle Request
        if (getArguments() != null) {
            String bundleRequest = getArguments().getString(WORKTYPE);
            // 接收update要求
            if (bundleRequest == UPDATE) {
                Log.d(TAG,"bundleRequest : "+UPDATE);
                if( getArguments().getString(NAME).trim() != null){
                    String name = getArguments().getString(NAME);
                    etName.setText(name);
                    load();
                }
            }
            // 接收createnew要求
            if(bundleRequest == CREATENEW){
                Log.d(TAG,"bundleRequest : "+CREATENEW);
                createNew();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.findViewById(R.id.cloudListFragment).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.mitList).setVisibility(View.INVISIBLE);
    }

    //1:createNew  2:增加 1 photoPosition 3:LoadSave檔
    private void upDateCurrentEditList(String requestCode,String s, Integer position) {
        switch (requestCode){
            case CREATENEW:
                currentEditList = new ArrayList<>();
                currentEditList.add(EMPTY);
                break;
            case ADD:
                currentEditList.add(EMPTY);
                break;
            case SAVE:
                currentEditList.set(position,s);
                break;
        }
    }

    private void handleButton() {

        ibBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mitEdit_to_mitList);
        });

        ibCreateNew.setOnClickListener(v ->{
            createNew();
        });

        ibAddPhoto.setOnClickListener(v -> {
            upDateCurrentEditList(ADD,null,null);
            viewPager2.setAdapter(myViewPager2Adapter);
            viewPager2.setCurrentItem(viewPager2.getAdapter().getItemCount());
        });

        ibDeletePhoto.setOnClickListener(v -> {
           currentEditList.remove(viewPager2.getCurrentItem());
            viewPager2.setAdapter(myViewPager2Adapter);
            viewPager2.setCurrentItem(viewPager2.getAdapter().getItemCount());
        });

        //測試上傳Storage
        ibUploadCould.setOnClickListener(v -> {
            Member member = loadMemberFromEdit(etName.getText().toString().trim());
                    uploadPhotoToCloud(member);
                });

        handleBtload();
        handleBtTakePic();
        handleBtSave();
        handleBtDelete();
    }

    private void uploadPhotoToCloud(Member member) {
        if(member ==null){
            Toast.makeText(activity, R.string.file_nodata, Toast.LENGTH_SHORT).show();
            return ;
        }

        File file = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        List<String> cloudList = new ArrayList<>();
        for(int i=0; i<member.getLocalChildPathList().size();i++){

            File childFile = new File(file, member.getLocalPhotoParentPath()+"/"+member.getLocalChildPathList().get(i));
            Uri sourceUri = Uri.fromFile(childFile);
            String imagePath = getString(R.string.app_name) + member.getCloudPhotosParentPath()+"/"+member.getLocalChildPathList().get(i);
            Log.d(TAG,"Storage imagePath : "+imagePath);
            cloudList.add(imagePath);
            storage.getReference().child(imagePath).putFile(sourceUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "putFile : isSuccessful");
                        } else {
                            Log.d(TAG, "putFile : fail");
                        }
                    });
        }
        member.setCloudChildPhotosPathList(cloudList);
        //要修改Nickname
        db.collection(getString(R.string.app_name)).document(CURRENTNICKNAME)
                .collection(CURRENTNICKNAME)
                .document(member.getStringName().toString())
                .set(member)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG,"upload DB : isSuccessful");
                        tvEditMessage.setText("uploadPhoto : isSuccessful");
                    }else {
                        Log.d(TAG,"upload DB : fail");
                        tvEditMessage.setText("uploadPhoto : fail");
                    }
                });

    }

    private void createNew() {
        upDateCurrentEditList(CREATENEW,null,0);
        etName.setText("");
        etMessage.setText("");
        viewPager2.setAdapter(myViewPager2Adapter);
    }

    private void handleAutoCompleteTextView() {
            upDateMemberNameList();
            if(Objects.equals(null,memberListEdit)){
                return;
            }
            List<String> listTemp = new ArrayList<>();

            for(String temp: memberListEdit){

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
            Log.d(TAG,"Edit delete :"+getString(R.string.FileisNull));
            return;
        }
        upDateMemberNameList();
        if(memberListEdit.size()>0){
            for (int i = 0; i < memberListEdit.size(); i++) {
                if(Objects.equals(etName.getText().toString(),memberListEdit.get(i).toString())){
                    position = i;
                    flag = true;
                    deleteName = etName.getText().toString().trim();
                    break;
                }
            }
            if(!flag){
                Log.d(TAG,"Edit delete : "+ getString(R.string.nofile));
                tvEditMessage.setText(R.string.nofile);
            }
        }
        // 有該member資料
        if ((position >= 0) && flag){
            file =new File(myDirDocument,LOCALNICKNAME+"/"+deleteName);
            File[] subFileList = file.listFiles();
            if(subFileList != null){
                for(File temp : subFileList) {
                    temp.delete();
                }
            }
            //再刪除Object資料
            if(file.delete()){
                Log.d(TAG,"Edit delete : "+ getString(R.string.delete));
                tvEditMessage.setText(R.string.delete);
                    File fileObjectPath = new File(activity.getFilesDir(), deleteName);
                    fileObjectPath.delete();
                etName.setText("");
                etMessage.setText("");
                upDateCurrentEditList(CREATENEW,null,null);
            }
        }
    }

    private void handleBtDelete() {
        ibDelete.setOnClickListener(v -> {
            delete();
            Navigation.findNavController(v).navigate(R.id.action_mitEdit_to_mitList);
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
            Log.d(TAG,"load : "+ getString(R.string.name_nofind));
            tvEditMessage.setText(getString(R.string.name_nofind));
            return;
        }
        StringBuilder sbTemp = new StringBuilder(sName);
        Member member = loadMemberFromEdit(sbTemp.toString());


        if(member ==null){
            Toast.makeText(activity, R.string.file_nodata, Toast.LENGTH_SHORT).show();
            return ;
        }
        StringBuilder s;
        currentEditList = new ArrayList<>();
        currentEditList = member.getLocalPhotosPathList();
        if(currentEditList.size() > 0){
            if(!Objects.equals(currentEditList,null)  && ( currentEditList.size() > 0) ){
                viewPager2.setAdapter(myViewPager2Adapter);
            }
        }
        etName.setText(member.getStringName());
        etMessage.setText(member.getStringMessage());

    }

    private void handleBtSave() {

        ibSave.setOnClickListener(v ->  {
            String sCheck =etName.getText().toString();
            if(Objects.equals(sCheck,null) || Objects.equals(sCheck,"") ){
                tvEditMessage.setText(R.string.name_null);

            }else{
                save();
                currentEditList = null;
                Navigation.findNavController(v).navigate(R.id.action_mitEdit_to_mitList);
                handleAutoCompleteTextView();
            }
        });
    }

    private void save(){
        //檢查是否已有此Name member
        StringBuilder sbNamePath = new StringBuilder(String.valueOf(etName.getText()).trim());
        if( sbNamePath.equals(null) ) {
            tvEditMessage.setText(getString(R.string.name_null));
        }
        //更新本機端 MemberList files[]去取Photo名字串
        upDateMemberNameList();

        File fileMember = new File(sbNamePath.toString().trim());
        //取得該Memner物件
        Member mCheck = loadMemberFromEdit(fileMember.toString());

        if(mCheck == null){
            Log.d(TAG,"Save File : "+getString(R.string.create_new));
            tvEditMessage.setText(getString(R.string.create_new));
        }else{
            Log.d(TAG,"Save File : "+getString(R.string.modify_file));
            tvEditMessage.setText(getString(R.string.modify_file));
        }
        //檢查編輯清單是否有值
        if( Objects.equals(currentEditList,null)
                || (currentEditList.size()<=0)
                || (Objects.equals(currentEditList.get(0), EMPTY)) ) {
            Log.d(TAG,"Save : "+getString(R.string.file_nodata));
            tvEditMessage.setText(getString(R.string.file_nodata));
            return;
        }

        try(
                //Object儲存資料夾
                FileOutputStream fos = activity.openFileOutput(sbNamePath.toString(),MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        )
        {
            Member member = new Member();
            StringBuilder sbName = new StringBuilder(String.valueOf(etName.getText()).trim());
            StringBuilder sbMessage = new StringBuilder(String.valueOf(etMessage.getText()).trim());
            dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            //建立資料夾，本機使用LOCALNAME
            String localParentPath = "/"+LOCALNICKNAME+"/"+sbNamePath.toString();
            dirMember = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS +localParentPath);

            int count = 0;
            File fileCrop = null ;
            File fileOrigin = null ;
            List<String> stringList = new ArrayList<>();
            List<String> stringChildList = new ArrayList<>();

            for(int i=0; i < currentEditList.size(); i++){
                if( Objects.equals(currentEditList.get(0), EMPTY) ) {
                    break;
                }
                StringBuilder stringCount = new StringBuilder();
                if(count<10){
                    stringCount.append("0");
                }
                stringCount.append(""+count);
                fileCrop =new File(dir,""+takePicCrop+ "_" + count +".jpg");
                fileOrigin =new File(dir,""+takePicOrigin+ "_" + count +".jpg");
                stringChildList.add(etName.getText().toString() +"_"+ stringCount+".jpg");
                fileDest = new File(dirMember,stringChildList.get(i));
                //pathList

                //先考慮裁切檔案
                if( currentEditList.get(i).contains("Crop") ){
                        copyPicture(fileCrop, new StringBuilder(fileDest.toString()));
                }
                fileCrop.delete();
                fileOrigin.delete();
                //本機child_Path
                stringList.add(fileDest.toString());
                count++;
            }
            //存本機Path
            member.setLocalPhotosPathList(stringList);

            member.setLocalPhotoParentPath(localParentPath);
            member.setCloudPhotosParentPath("/"+CURRENTNICKNAME+"/"+sbNamePath.toString());

            member.setLocalChildPathList(stringChildList);
            member.setStringName(sbName.toString());
            member.setStringMessage(sbMessage.toString());
            oos.writeObject(member);

            etName.setText(null);
            etMessage.setText(null);
            // 同步上傳
            if(cbAutoSync.isChecked()){
                uploadPhotoToCloud(member);
            }
            Log.d(TAG,"Save : " + getString(R.string.save));
            tvEditMessage.setText(getString(R.string.save));

        }catch (Exception e){
            Log.e(TAG,e.toString());
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }
    }

    private Member loadMemberFromEdit(String string) {
        try(
                FileInputStream fis = activity.openFileInput(string);
                ObjectInputStream ois = new ObjectInputStream(fis);
        )
        {
            return (Member) ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    private void upDateMemberNameList() {
        File f = new File(myDirDocument.toString()+"/"+ LOCALNICKNAME);
        if(!f.exists()){
            return;
        }
        File[] files= f.listFiles();

        memberListEdit.clear();
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                memberListEdit.add(String.valueOf(new StringBuilder(files[i].getName().trim())));
            }
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
                        upDateCurrentEditList(SAVE,fliePathTemp.toString(),pagerNumber);

                        viewPager2.setAdapter(myViewPager2Adapter);
                        viewPager2.setCurrentItem( viewPager2.getAdapter().getItemCount() );

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
        tvEditMessage = view.findViewById(R.id.tvMessage_Edit);
        etMessage = view.findViewById(R.id.etMessage);
        ibSave = view.findViewById(R.id.ibSaveMember);
        ibLoad = view.findViewById(R.id.ibLoad);
        ibDelete = view.findViewById(R.id.ibDeleteMember);
        ibUploadCould = view.findViewById(R.id.ibUploadCloud);
        ibCreateNew = view.findViewById(R.id.ibCreateNewMember);

        ibAddPhoto = view.findViewById(R.id.ibAdd_photo);
        ibTakePic = view.findViewById(R.id.ibCamera_photo);
        ibDeletePhoto = view.findViewById(R.id.ibDelete_photo);
        ibSelectPhoto =view.findViewById(R.id.ibSelectFile_photo);

        ibBack = view.findViewById(R.id.ibBack_Edit);
        cbAutoSync = view.findViewById(R.id.cbAutoSyncCloud);

        viewPager2 = view.findViewById(R.id.viewPager2);

    }

    private void handleInitialAndVisibility() {
        myViewPager2Adapter = new MyViewPager2Adapter(this, currentEditList);
        viewPager2.setAdapter(myViewPager2Adapter);
    }

    private void handleBtTakePic() {
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


    class MyViewPager2Adapter extends FragmentStateAdapter {
        private List<String> list;

        public MyViewPager2Adapter(@NonNull Fragment fragment, List<String> list) {
            super(fragment);
            this.list = list;
        }

        public void setMyViewPager2Adapter(List<String> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
           String s = new String(currentEditList.get(position));
           return new AddLocalPhotoFragment(s);
        }

        @Override
        public int getItemCount() {
            return currentEditList == null ? 0 : currentEditList.size();
        }
    }

}