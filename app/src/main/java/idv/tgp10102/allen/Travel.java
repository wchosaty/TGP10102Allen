package idv.tgp10102.allen;

import java.io.Serializable;

public class Travel implements Serializable {
    private StringBuilder stringMessage;
    private StringBuilder stringName;
    private StringBuilder stringFilePath1,stringFilePath2,stringFilePath3,stringFilePath4;

    public StringBuilder getStringFilePath1() {
        return stringFilePath1;
    }

    public void setStringFilePath1(StringBuilder stringFilePath1) {
        this.stringFilePath1 = stringFilePath1;
    }

    public StringBuilder getStringFilePath2() {
        return stringFilePath2;
    }

    public void setStringFilePath2(StringBuilder stringFilePath2) {
        this.stringFilePath2 = stringFilePath2;
    }

    public StringBuilder getStringFilePath3() {
        return stringFilePath3;
    }

    public void setStringFilePath3(StringBuilder stringFilePath3) {
        this.stringFilePath3 = stringFilePath3;
    }

    public StringBuilder getStringFilePath4() {
        return stringFilePath4;
    }

    public void setStringFilePath4(StringBuilder stringFilePath4) {
        this.stringFilePath4 = stringFilePath4;
    }

    public StringBuilder getStringName() {
        return stringName;
    }

    public void setStringName(StringBuilder stringName) {
        this.stringName = stringName;
    }

    public StringBuilder getStringMessage() {
        return stringMessage;
    }

    public void setStringMessage(StringBuilder stringMessage) {
        this.stringMessage = stringMessage;
    }
}
