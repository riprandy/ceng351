package ceng.ceng351.ModelHubPlatform;

public class Model {

    private int ModelID;
    private String model_name;
    private String license;
    private String size;
    private int OrgID;

    public Model(int ModelID, String model_name, String license, String size, int OrgID) {
        this.ModelID = ModelID;
        this.model_name = model_name;
        this.license = license;
        this.size = size;
        this.OrgID = OrgID;
    }

    public int getModelID() {
        return ModelID;
    }

    public void setModelID(int ModelID) {
        this.ModelID = ModelID;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getOrgID() {
        return OrgID;
    }

    public void setOrgID(int OrgID) {
        this.OrgID = OrgID;
    }

    @Override
    public String toString() {
        return "Model{" +
                "ModelID=" + ModelID +
                ", model_name='" + model_name + '\'' +
                ", license='" + license + '\'' +
                ", size='" + size + '\'' +
                ", OrgID=" + OrgID +
                '}';
    }
}