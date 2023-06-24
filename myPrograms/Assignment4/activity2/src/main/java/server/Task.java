package server;

public class Task {
    private String task;
    private String answer;

    public Task(String task, String answer) {
        this.task = task;
        this.answer = answer;
    }

    public String getTask() {
        return task;
    }

    public String getAnswer() {
        return answer;
    }
}
