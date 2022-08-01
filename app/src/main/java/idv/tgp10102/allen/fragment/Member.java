package idv.tgp10102.allen.fragment;

import java.io.Serializable;
import java.util.List;

public class Member implements Serializable {
    private StringBuilder stringMessage;
    private StringBuilder stringName;
    private StringBuilder stringPhotosPath;
    private List<String> myPhotosPathList;
    private String nickname;

    public List<String> getMyPhotosPashList() {
        return myPhotosPathList;
    }

    public void setMyPhotosPashList(List<String> myPhotosPathList) {
        this.myPhotosPathList = myPhotosPathList;
    }

    public StringBuilder getStringMessage() {
        return stringMessage;
    }

    public void setStringMessage(StringBuilder stringMessage) {
        this.stringMessage = stringMessage;
    }

    public StringBuilder getStringName() {
        return stringName;
    }

    public void setStringName(StringBuilder stringName) {
        this.stringName = stringName;
    }

    public StringBuilder getStringPhotosPath() {
        return stringPhotosPath;
    }

    public void setStringPhotosPath(StringBuilder stringPhotosPath) {
        this.stringPhotosPath = stringPhotosPath;
    }
}
