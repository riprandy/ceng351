package ceng.ceng351.ModelHubPlatform;

public class follow {

    private int followerPIN;
    private int followeePIN;
    private String following_date;

    public follow(int followerPIN, int followeePIN, String following_date) {
        this.followerPIN = followerPIN;
        this.followeePIN = followeePIN;
        this.following_date = following_date;
    }

    public int getFollowerPIN() {
        return followerPIN;
    }

    public void setFollowerPIN(int followerPIN) {
        this.followerPIN = followerPIN;
    }

    public int getFolloweePIN() {
        return followeePIN;
    }

    public void setFolloweePIN(int followeePIN) {
        this.followeePIN = followeePIN;
    }

    public String getFollowing_date() {
        return following_date;
    }

    public void setFollowing_date(String following_date) {
        this.following_date = following_date;
    }

    @Override
    public String toString() {
        return "follow{" +
                "followerPIN=" + followerPIN +
                ", followeePIN=" + followeePIN +
                ", following_date='" + following_date + '\'' +
                '}';
    }
}
