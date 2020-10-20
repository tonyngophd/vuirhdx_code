package com.suas.uxdual;

public class IRCamera {
    public final int VuePro = 0;
    public final int Boson = 1;
    public final int Tau2 = 2;
    public final int Lepton3 = 3;
    public final int Lepton35 = 4;
    public final int Unknown = 999;

    private String cameraName = "Unknown";
    private int cameraClass = Unknown;
    private int cID = 0;
    private int[] cResolution = {320, 256};

    /*public IRCamera(String Name, int Class, int id, int[] res){
        this.cameraName = Name;
        this.cameraClass = Class;
        this.cID = id;
        this.cResolution[0] = res[0];
        this.cResolution[1] = res[1];
    }*/

    public String getName(){
        return cameraName;
    }

    public int getCameraClass(){
        return cameraClass;
    }

    public int getID(){
        return cID;
    }

    public int[] getResolution(){
        return cResolution;
    }

    public void setName(String name){
        this.cameraName = name;
    }

    public void setID(int id){
        this.cID = id;
    }

    public void setCameraClass(int cameraClass){
        this.cameraClass = cameraClass;
    }

    public void setResolution(int[] resolution){
        this.cResolution[0] = resolution[0];
        this.cResolution[1] = resolution[1];
    }
    public void setIRCamera(String Name, int Class, int id, int[] res){
        this.cameraName = Name;
        this.cameraClass = Class;
        this.cID = id;
        this.cResolution[0] = res[0];
        this.cResolution[1] = res[1];
    }
}
