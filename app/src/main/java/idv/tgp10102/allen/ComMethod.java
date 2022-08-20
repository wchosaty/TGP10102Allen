package idv.tgp10102.allen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ComMethod {
    private static final String TAG = "Tag_ComMethod";
    public static List<String> memberStringList;

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
            return list;
        }
        for (int i = 0; i < ComMethod.memberStringList.size(); i++) {

            String sbTemp = String.valueOf(ComMethod.memberStringList.get(i));
            Member member = ComMethod.loadMember(context,sbTemp);
            //memberString(成員名稱)轉換出memberObjects(物件)
            list.add(member);
        }
        return list;
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
