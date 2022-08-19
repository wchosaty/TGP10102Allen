package idv.tgp10102.allen;

import java.io.Serializable;
import java.util.List;

public class NicknameList implements Serializable {
    private List<String> list;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
