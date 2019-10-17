package com.envisioniot.enos.iot_mqtt_sdk.core.exception;

import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;

/**
 * @author zhensheng.cai
 * @date 2018/7/3
 */
public class EnvisionException extends Exception {
    private static final long serialVersionUID = 5874811335473710877L;

    private static final int NO_AVAIL_ERROR_CODE = -99999;

    private final int errorCode;
    private final String errorMessage;

    public EnvisionException(String errorMessage) {
        this(NO_AVAIL_ERROR_CODE, errorMessage);
    }

    /**
     * Without known exception cause
     */
    public EnvisionException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * With known exception cause
     */
    public EnvisionException(String message, Throwable cause, int errorCode, String errorMessage) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public EnvisionException(Throwable cause, int errorCode, String errorMessage) {
        this(errorMessage, cause, errorCode, errorMessage);
    }

    public EnvisionException(Throwable cause, IEnvisionError error) {
        this(error.getErrorMessage(), cause, error);
    }

    public EnvisionException(String message, Throwable cause, IEnvisionError error) {
        this(message, cause, error.getErrorCode(), error.getErrorMessage());
    }

    public EnvisionException(IEnvisionError error) {
        this(error, null);
    }

    public EnvisionException(IEnvisionError error, String extraMsg) {
        this(error.getErrorCode(), combinedErrorMessage(error, extraMsg));
    }

    private static String combinedErrorMessage(IEnvisionError error, String extraMsg) {
        if (StringUtil.isNotEmpty(extraMsg)) {
            return error.getErrorMessage() + "(" + extraMsg + ")";
        }
        return error.getErrorMessage();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
