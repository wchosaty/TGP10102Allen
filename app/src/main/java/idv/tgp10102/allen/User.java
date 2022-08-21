package idv.tgp10102.allen;

import java.io.Serializable;

public class User implements Serializable {
    private String email,uid,phone,nickName,nicknameCloudPic;

    public String getNicknameCloudPic() {
        return nicknameCloudPic;
    }

    public void setNicknameCloudPic(String nicknameCloudPic) {
        this.nicknameCloudPic = nicknameCloudPic;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
