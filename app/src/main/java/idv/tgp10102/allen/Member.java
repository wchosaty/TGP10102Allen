package idv.tgp10102.allen;

import java.io.Serializable;
import java.util.List;

public class Member implements Serializable {
    private String stringMessage;
    private String stringName;
    private String cloudPhotosParentPath;
    private List<String> cloudChildPhotosPathList;
    private String cloudDBParentPath;
    private String localPhotoParentPath;
    private List<String> localPhotosPathList;
    private List<String> localChildPathList;
    private String nickname;

    public List<String> getLocalChildPathList() {
        return localChildPathList;
    }

    public void setLocalChildPathList(List<String> localChildPathList) {
        this.localChildPathList = localChildPathList;
    }

    public String getStringMessage() {
        return stringMessage;
    }

    public String getLocalPhotoParentPath() {
        return localPhotoParentPath;
    }

    public void setLocalPhotoParentPath(String localPhotoParentPath) {
        this.localPhotoParentPath = localPhotoParentPath;
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

    public String getCloudPhotosParentPath() {
        return cloudPhotosParentPath;
    }

    public void setCloudPhotosParentPath(String cloudPhotosParentPath) {
        this.cloudPhotosParentPath = cloudPhotosParentPath;
    }

    public List<String> getCloudChildPhotosPathList() {
        return cloudChildPhotosPathList;
    }

    public void setCloudChildPhotosPathList(List<String> cloudChildPhotosPathList) {
        this.cloudChildPhotosPathList = cloudChildPhotosPathList;
    }

    public String getCloudDBParentPath() {
        return cloudDBParentPath;
    }

    public void setCloudDBParentPath(String cloudDBParentPath) {
        this.cloudDBParentPath = cloudDBParentPath;
    }

    public List<String> getLocalPhotosPathList() {
        return localPhotosPathList;
    }

    public void setLocalPhotosPathList(List<String> localPhotosPathList) {
        this.localPhotosPathList = localPhotosPathList;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
