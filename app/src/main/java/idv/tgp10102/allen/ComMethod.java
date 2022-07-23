package idv.tgp10102.allen;

import static idv.tgp10102.allen.MainActivity.FILENAME;
import static idv.tgp10102.allen.MainActivity.LISTMAP_NAME;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map;

public class ComMethod {

    private static final String TAG = "Tag_ComMethod";

    public static void countMember(){

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
