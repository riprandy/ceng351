package ceng.ceng351.ModelHubPlatform;

public class Task {

    private int TaskID;
    private String task_name;

    public Task(int TaskID, String task_name) {
        this.TaskID = TaskID;
        this.task_name = task_name;
    }

    public int getTaskID() {
        return TaskID;
    }

    public void setTaskID(int TaskID) {
        this.TaskID = TaskID;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    @Override
    public String toString() {
        return "Task{" +
                "TaskID=" + TaskID +
                ", task_name='" + task_name + '\'' +
                '}';
    }
}