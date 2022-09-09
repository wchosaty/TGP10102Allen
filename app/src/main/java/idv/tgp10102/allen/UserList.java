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
    private String content;

    public UserList(String content ,ExecutorService executor,ImageView imageView) {
        this.content = content;
        this.executorService = executor;
        this.imageView = imageView;
        searchUserPath();
    }


    public void searchUserPath () {
            FirebaseFirestore.getInstance().collection("TGP101 02 Allenusers")
                    .get().addOnCompleteListener(taskUser -> {
                        if (taskUser.isSuccessful() && taskUser.getResult() != null) {
                            for (QueryDocumentSnapshot documentSnapshot : taskUser.getResult()) {
                                User userTemp = documentSnapshot.toObject(User.class);
//                            Log.d(TAG, "nickname : " + userTemp.getNickName());
                                if(Objects.equals(content,userTemp.getNickName())){
                                    toAccessCallable("TGP101 02 Allen/userPicture/"+userTemp.getUid());
                                }
                            }
                        } else {
                            Log.e(TAG, "taskUser : Download Fail");
                        }
                    });

    }

//    public String searchUserPath(String searchName) {
//        for (int i = 0; i < mapList.size(); i++) {
//            if ( Objects.equals(searchName, mapList.get(i).get("name")) ) {
//                return "TGP101 02 Allen/userPicture/" + mapList.get(i).get("uid");
//            }
//        }
//        return "";
//    }

    public void toAccessCallable (String path){
        new AccessCallable().getViewPicture(path,executorService,imageView);
    }
}
