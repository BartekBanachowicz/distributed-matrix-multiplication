package put.poznan.guiclient;

import javafx.fxml.FXML;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

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

    public void setListOfUnits(String[] units){
        controller.setListOfUnits(units);
    }

    public void changeProgress(double progress){
        controller.changeProgress(progress);
    }

}
