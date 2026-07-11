package pojos.result;

/**
 * Created by Raghu Teja on 27-06-2017.
 */
public class FileUploadRes {

    @SuppressWarnings("WeakerAccess")
    public boolean success;
    @SuppressWarnings("WeakerAccess")
    public String fileName;

    public FileUploadRes() {
        this.success = false;
    }

    public FileUploadRes(String fileName) {
        this.success = true;
        this.fileName = fileName;
    }
}
