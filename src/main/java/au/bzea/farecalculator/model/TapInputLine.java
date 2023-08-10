package au.bzea.farecalculator.model;

public class TapInputLine { 
    public int ID;

    public String DateTimeUTC;

    public String TapType;

    public String StopId;

    public String CompanyId;

    public String BusID;

    public String PAN;
    
    public String Status;

    public void setID(int ID) { this.ID = ID; }

    public int getID() { return this.ID; }

    public void setDateTimeUTC(String DateTimeUTC) { this.DateTimeUTC = DateTimeUTC; }

    public String getDateTimeUTC() { return this.DateTimeUTC; }

    public void setTapType(String TapType) { this.TapType = TapType; }

    public String getTapType() { return this.TapType; }

    public void setStopId(String StopId) { this.StopId = StopId; }

    public String getStopId() { return this.StopId; }

    public void setCompanyId(String CompanyId) { this.CompanyId = CompanyId; }

    public String getCompanyId() { return this.CompanyId; }

    public void setBusID(String BusID) { this.BusID = BusID; }

    public String getBusID() { return this.BusID; }

    public void setPAN(String PAN) { this.PAN = PAN; }

    public String getPAN() { return this.PAN; }

    
}