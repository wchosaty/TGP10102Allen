package idv.tgp10102.allen;


import static idv.tgp10102.allen.MainActivity.LOCALNICKNAME;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.fragment.Member;


public class ComMethod {
    private static final String TAG = "Tag_ComMethod";
    public static List<String> memberStringList;
    private File dirMember;

    public static Bitmap bitmapToImageFilePath(Bitmap bitmap, File filepath) throws IOException {
        ImageDecoder.Source source = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            source = ImageDecoder.createSource(filepath);
            bitmap = ImageDecoder.decodeBitmap(source);
        }else{
            bitmap = BitmapFactory.decodeFile(filepath.toString());
        }
        return bitmap;
    }
    public static List<Member> getMemberObjectsList(Context context) {
        List<Member> list = new ArrayList<>();
        if(Objects.equals(ComMethod.memberStringList,null) ||
                ComMethod.memberStringList.size() <= 0){
            return null;
        }
        for (int i = 0; i < ComMethod.memberStringList.size(); i++) {

            StringBuilder sbTemp = new StringBuilder(String.valueOf(ComMethod.memberStringList.get(i)));
            Member member = ComMethod.loadMember(context,sbTemp.toString());
            //memberString(成員名稱)轉換出memberObjects(物件)
            list.add(member);
        }
        return list;
    }

    public static void getMemberStringList(Context context){
//        File dirMember = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File f = new File(MainActivity.myDirMember.toString()+"/"+LOCALNICKNAME);
        if(!f.exists()){
            return;
        }
        File[] files= f.listFiles();

        memberStringList = new ArrayList<>();
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                memberStringList.add(String.valueOf(new StringBuilder(files[i].getName().trim())));
            }
        }
    }
    public static Member loadMember(Context context, String s){
        try(
                FileInputStream fis = context.openFileInput(s);
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

}
