package idv.tgp10102.allen;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AccessCallable {
    private static final String TAG = "Tag_AccessCallable";
    // 官方模擬器
    public static String SERVER_URL = "http://10.0.2.2:8080/PocketWebFcm/";
    // ngrok(模擬時機皆可用)
//    public static String SERVER_URL = "https://46e8-2001-b400-e30a-a0b2-3d50-3952-abba-89ba.jp.ngrok.io/PocketWebFcm/";
    // 區域網路
//    public static String SERVER_URL = "http://192.168.23.138:8080/PocketWebFcm/";


    public void getViewPicture(String path, ExecutorService executor, ImageView imageView) {
        ImageCallable imageCallable = new ImageCallable(path, imageView);
        Future<Boolean> future = executor.submit(imageCallable);
    }

    public void getCloudThumb(String nickname, String photoName,ImageView imageView, TextView textView,
                              ExecutorService executor, int code) {
        StringCallable stringCallable = new StringCallable(nickname,photoName,imageView,textView,code);
        Future<Boolean> futureCloudThumb = executor.submit(stringCallable);
    }
    public void getCloudComment(String nickName, String photoName,ImageView imageView, TextView textView,
                                ExecutorService executor, int code) {
        StringCallable stringCallable = new StringCallable(nickName,photoName,imageView,textView,code);
        Future<Boolean> futureCloudComment = executor.submit(stringCallable);
    }
    public void getJsonData(String url, String jsonString, ExecutorService executor){
        Log.d(TAG,"getJsonData URL: "+url);
        JsonCallable jsonCallable = new JsonCallable(url,jsonString);
        Future<String> futureJsonDate = executor.submit(jsonCallable);
    }

}
