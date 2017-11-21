/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cls;

/**
 *
 * @author Hades
 */
public class Ticket {

    private String barcode;
    private int entranceGate;
    private String entranceTime;
    private int exitGate;
    private String exitTime;
    private int price;
    private String paymentTime;
    private String paymentMethods;
    private String licenseNumber;
    private String vehicleTypes;
    private String tarifTypes;
    private String overNightParking;
    private String gateInPicture1;
    private String gateInPicture2;
    private String gateOutPicture1;
    private String gateOutPicture2;
    //Unusable variabel (yet)
    /*
     paidamount
     trobtix
     manualtix
     losttix
     cashierID
     */

    public Ticket() {
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getEntranceGate() {
        return entranceGate;
    }

    public void setEntranceGate(int entranceGate) {
        this.entranceGate = entranceGate;
    }

    public String getEntranceTime() {
        return entranceTime;
    }

    public void setEntranceTime(String entranceTime) {
        this.entranceTime = entranceTime;
    }

    public int getExitGate() {
        return exitGate;
    }

    public void setExitGate(int exitGate) {
        this.exitGate = exitGate;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(String paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getVehicleTypes() {
        return vehicleTypes;
    }

    public void setVehicleTypes(String vehicleTypes) {
        this.vehicleTypes = vehicleTypes;
    }

    public String getTarifTypes() {
        return tarifTypes;
    }

    public void setTarifTypes(String tarifTypes) {
        this.tarifTypes = tarifTypes;
    }

    public String getOverNightParking() {
        return overNightParking;
    }

    public void setOverNightParking(String overNightParking) {
        this.overNightParking = overNightParking;
    }

    public String getGateInPicture1() {
        return gateInPicture1;
    }

    public void setGateInPicture1(String gateInPicture1) {
        this.gateInPicture1 = gateInPicture1;
    }

    public String getGateInPicture2() {
        return gateInPicture2;
    }

    public void setGateInPicture2(String gateInPicture2) {
        this.gateInPicture2 = gateInPicture2;
    }

    public String getGateOutPicture1() {
        return gateOutPicture1;
    }

    public void setGateOutPicture1(String gateOutPicture1) {
        this.gateOutPicture1 = gateOutPicture1;
    }

    public String getGateOutPicture2() {
        return gateOutPicture2;
    }

    public void setGateOutPicture2(String gateOutPicture2) {
        this.gateOutPicture2 = gateOutPicture2;
    }

    @Override
    public String toString() {
        return "Ticket{" + "barcode=" + barcode + ", entranceGate=" + entranceGate + ", entranceTime=" + entranceTime + ", exitGate=" + exitGate + ", exitTime=" + exitTime + ", price=" + price + ", paymentTime=" + paymentTime + ", paymentMethods=" + paymentMethods + ", licenseNumber=" + licenseNumber + ", vehicleTypes=" + vehicleTypes + ", tarifTypes=" + tarifTypes + ", overNightParking=" + overNightParking + ", gateInPicture1=" + gateInPicture1 + ", gateInPicture2=" + gateInPicture2 + ", gateOutPicture1=" + gateOutPicture1 + ", gateOutPicture2=" + gateOutPicture2 + '}';
    }

}
