package pojos.result;

/**
 * Created by Raghu Teja on 28-07-2017.
 */
public class ReindexRes {
    @SuppressWarnings("WeakerAccess")
    public boolean success;
    @SuppressWarnings("WeakerAccess")
    public String message;

    public ReindexRes(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ReindexRes(String message) {
        this(true, message);
    }
}
