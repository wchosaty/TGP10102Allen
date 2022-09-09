package idv.tgp10102.allen;


public class SortObject implements Comparable<SortObject> {
    private Integer count;
    private String stringContent;

    public SortObject(Integer count, String stringContent) {
        this.count = count;
        this.stringContent = stringContent;
    }

    public SortObject(String stringContent) {
        this.stringContent = stringContent;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getStringContent() {
        return stringContent;
    }

    public void setStringContent(String stringContent) {
        this.stringContent = stringContent;
    }


    @Override
    public int compareTo(SortObject o) {
        return o.getCount() - count;
    }
}