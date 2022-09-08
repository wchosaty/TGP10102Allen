package idv.tgp10102.allen;

import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


public class UserList {
    private static final String TAG = "Tag_UserList";
    private ImageView imageView;
    private ExecutorService executorService;
    private List<Map<String,String>> mapList;
    private String name;

    public UserList(ImageView imageView, ExecutorService executorService, String name,int code) {
        this.imageView = imageView;
        this.executorService = executorService;
        this.name = name;
        mapList = new ArrayList<>();
        switch (code){
            case 1:
                searchUser();
            break;
        }
    }

    public void searchUser() {
        Map<String,String> searchUid = new HashMap<>();
        Map<String,String> map = new HashMap<>();
        // 讀取帳戶
        FirebaseFirestore.getInstance().collection("TGP101 02 Allenusers")
                .get().addOnCompleteListener(taskUser -> {
                    if (taskUser.isSuccessful() && taskUser.getResult() != null) {

                        for(QueryDocumentSnapshot documentSnapshot : taskUser.getResult() ){
                            User userTemp = documentSnapshot.toObject(User.class);
                            Log.d(TAG , "nickname : " +userTemp.getNickName());
                            if( Objects.equals(name,userTemp.getNickName()) ){
                                searchUid.put(name,"TGP101 02 Allen/userPicture/"+userTemp.getUid());
                            }
                            map.put("name",userTemp.getNickName());
                            map.put("uid",userTemp.getUid());
                            map.put("path","TGP101 02 Allen/userPicture/"+userTemp.getNicknameCloudPic());
                            mapList.add(map);
                        }
                        String path = searchUid.get(name);
                        new AccessCallable().getViewPicture(path,executorService,imageView);
                    } else {
                        Log.e(TAG, "taskUser : Download Fail");
                    }
                });
    }

    public void AccessCallable(String name,String path){
        new AccessCallable().getViewPicture(path,executorService,imageView);
    }

}
