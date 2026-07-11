package uicom.response;

import org.apache.commons.lang.StringUtils;

public class JSONResponse {

	private Object result;
	private String errorMessage;
	private String errorCode;

	public JSONResponse(Object result, String errorMessage, String errorCode) {
		super();
		this.result = result;
		this.errorMessage = errorMessage;
		this.errorCode = errorCode;
	}
	
	public JSONResponse(Object result) {
		this(result, StringUtils.EMPTY, StringUtils.EMPTY);
	}
	
	public JSONResponse(ErrorInfo errorInfo) {
		this(StringUtils.EMPTY, errorInfo.getErrorMessage(), errorInfo.getErrorCode());
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
