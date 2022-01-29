package put.poznan.guiclient;

public class GUIAdapter {

    private final GUIClientController controller;

    GUIAdapter(GUIClientController controller){
        this.controller = controller;
    }

    public void setConnected(){
        controller.setConnected();
    }

    public void setConnecting(){
        controller.setConnecting();
    }

    public void setReady(){
        controller.setReady();
    }

    public void setRunning(){
        controller.setRunning();
    }

    public void setStopped(){
        controller.setStopped();
    }

    public void setRefused(){
        controller.setRefused();
    }

    public void setListOfUnits(String[] units){
        controller.setListOfUnits(units);
    }

    public void changeProgress(double progress){
        controller.changeProgress(progress);
    }

}
