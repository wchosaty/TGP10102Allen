package idv.tgp10102.allen;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ComMethod {
    private static final String TAG = "Tag_ComMethod";
    public static List<String> memberList;
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

    public static void getMemberList(Context context){
        File dirMember = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File f = new File(dirMember.toString());
        File[] files= f.listFiles();
        memberList = new ArrayList<>();
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                memberList.add(String.valueOf(new StringBuilder(files[i].getName().trim())));
            }
        }
    }

    public static Travel loadTravel(Context context,String s){
        try(
                FileInputStream fis = context.openFileInput(s);
                ObjectInputStream ois = new ObjectInputStream(fis);
        )
        {
            return (Travel) ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            return null;
        }
    }
}
