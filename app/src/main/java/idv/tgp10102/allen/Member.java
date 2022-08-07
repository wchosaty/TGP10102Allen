package idv.tgp10102.allen;

import java.io.Serializable;
import java.util.List;

public class Member implements Serializable {
    private String stringMessage;
    private String stringName;
    private String stringPhotosPath;
    private List<String> myPhotosPathList;
    private String nickname;

    public List<String> getMyPhotosPashList() {
        return myPhotosPathList;
    }

    public List<String> getMyPhotosPathList() {
        return myPhotosPathList;
    }

    public void setMyPhotosPathList(List<String> myPhotosPathList) {
        this.myPhotosPathList = myPhotosPathList;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setMyPhotosPashList(List<String> myPhotosPathList) {
        this.myPhotosPathList = myPhotosPathList;
    }

    public String getStringMessage() {
        return stringMessage;
    }

    public void setStringMessage(String stringMessage) {
        this.stringMessage = stringMessage;
    }

    public String getStringName() {
        return stringName;
    }

    public void setStringName(String stringName) {
        this.stringName = stringName;
    }

    public String getStringPhotosPath() {
        return stringPhotosPath;
    }

    public void setStringPhotosPath(String stringPhotosPath) {
        this.stringPhotosPath = stringPhotosPath;
    }
}
