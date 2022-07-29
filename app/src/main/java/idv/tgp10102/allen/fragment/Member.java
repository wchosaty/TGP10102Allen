package idv.tgp10102.allen.fragment;

import java.io.Serializable;

public class Member implements Serializable {
    private StringBuilder stringMessage;
    private StringBuilder stringName;
    private StringBuilder stringPhotosPath;
    private Integer memberPhotoCount;

    public Integer getMemberPhotoCount() {
        return memberPhotoCount;
    }

    public void setMemberPhotoCount(Integer memberPhotoCount) {
        this.memberPhotoCount = memberPhotoCount;
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
