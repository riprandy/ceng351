package ceng.ceng351.ModelHubPlatform;

public class upload {

    private int PIN;
    private int DatasetID;
    private String role;

    public upload(int PIN, int DatasetID, String role) {
        this.PIN = PIN;
        this.DatasetID = DatasetID;
        this.role = role;
    }

    public int getPIN() {
        return PIN;
    }

    public void setPIN(int PIN) {
        this.PIN = PIN;
    }

    public int getDatasetID() {
        return DatasetID;
    }

    public void setDatasetID(int DatasetID) {
        this.DatasetID = DatasetID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "upload{" +
                "PIN=" + PIN +
                ", DatasetID=" + DatasetID +
                ", role='" + role + '\'' +
                '}';
    }
}
