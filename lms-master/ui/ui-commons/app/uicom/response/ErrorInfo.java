package uicom.response;

public class ErrorInfo {
    private String errorCode;
    private String errorMessage;

    public ErrorInfo(String errorCode) {
        this(errorCode, errorCode);
    }

    public ErrorInfo(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{");
        s.append("errorCode:").append(errorCode).append(", ");
        s.append("errorMessage:").append(errorMessage);
        s.append("}");
        return s.toString();
    }

}
