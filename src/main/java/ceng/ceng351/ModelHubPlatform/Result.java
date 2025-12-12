package ceng.ceng351.ModelHubPlatform;

public class Result {

    private int ResultID;
    private double accuracy;
    private double f1_score;
    private String hyperparameter_config;
    private int PIN;
    private int ModelID;
    private String version_no;
    private int DatasetID;

    public Result(int ResultID, double accuracy, double f1_score, String hyperparameter_config,
                  int PIN, int ModelID, String version_no, int DatasetID) {
        this.ResultID = ResultID;
        this.accuracy = Math.round(accuracy * 100.0) / 100.0;
        this.f1_score = Math.round(f1_score * 100.0) / 100.0;
        this.hyperparameter_config = hyperparameter_config;
        this.PIN = PIN;
        this.ModelID = ModelID;
        this.version_no = version_no;
        this.DatasetID = DatasetID;
    }

    public int getResultID() {
        return ResultID;
    }

    public void setResultID(int ResultID) {
        this.ResultID = ResultID;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getF1_score() {
        return f1_score;
    }

    public void setF1_score(double f1_score) {
        this.f1_score = f1_score;
    }

    public String getHyperparameter_config() {
        return hyperparameter_config;
    }

    public void setHyperparameter_config(String hyperparameter_config) {
        this.hyperparameter_config = hyperparameter_config;
    }

    public int getPIN() {
        return PIN;
    }

    public void setPIN(int PIN) {
        this.PIN = PIN;
    }

    public int getModelID() {
        return ModelID;
    }

    public void setModelID(int ModelID) {
        this.ModelID = ModelID;
    }

    public String getVersion_no() {
        return version_no;
    }

    public void setVersion_no(String version_no) {
        this.version_no = version_no;
    }

    public int getDatasetID() {
        return DatasetID;
    }

    public void setDatasetID(int DatasetID) {
        this.DatasetID = DatasetID;
    }

    @Override
    public String toString() {
        return "Result{" +
                "ResultID=" + ResultID +
                ", accuracy=" + String.format("%.2f", accuracy) +
                ", f1_score=" + String.format("%.2f", f1_score) +
                ", hyperparameter_config='" + hyperparameter_config + '\'' +
                ", PIN=" + PIN +
                ", ModelID=" + ModelID +
                ", version_no='" + version_no + '\'' +
                ", DatasetID=" + DatasetID +
                '}';
    }
}
