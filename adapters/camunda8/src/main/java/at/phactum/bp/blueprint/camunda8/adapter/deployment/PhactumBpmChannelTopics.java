package at.phactum.bp.blueprint.camunda8.adapter.deployment;

public class PhactumBpmChannelTopics {

    private String controlIn;

    private String controlOut;

    private String bpeIn;

    private String bpeOut;

    public String getControlIn() {
        return controlIn;
    }

    public void setControlIn(String controlIn) {
        this.controlIn = controlIn;
    }

    public String getControlOut() {
        return controlOut;
    }

    public void setControlOut(String controlOut) {
        this.controlOut = controlOut;
    }

    public String getBpeIn() {
        return bpeIn;
    }

    public void setBpeIn(String bpeIn) {
        this.bpeIn = bpeIn;
    }

    public String getBpeOut() {
        return bpeOut;
    }

    public void setBpeOut(String bpeOut) {
        this.bpeOut = bpeOut;
    }

}
