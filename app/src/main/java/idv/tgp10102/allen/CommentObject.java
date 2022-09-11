package idv.tgp10102.allen;

import java.io.Serializable;

public class CommentObject implements Serializable {
    private String nickname;
    private String comment;

    public CommentObject(String nickname, String comment) {
        this.nickname = nickname;
        this.comment = comment;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
