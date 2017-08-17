package main.demov1;

public class Reminder {
    private String title;
    private int hour;
    private int minute;
    private String reminderType;
    private String[][] dailyList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String[][] getDailyList() {
        return dailyList;
    }

    // Not implemented yet
    public void setDailyList(String[][] dailyList) {
        this.dailyList = dailyList;
    }

    public void addToDailyList(String[][] data) {

    }
}
