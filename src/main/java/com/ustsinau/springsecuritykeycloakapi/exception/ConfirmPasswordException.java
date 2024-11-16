package com.ustsinau.springsecuritykeycloakapi.exception;

public class ConfirmPasswordException extends ApiException{

    public ConfirmPasswordException(String message, String errorCode) {
        super(message, errorCode);
    }
}
