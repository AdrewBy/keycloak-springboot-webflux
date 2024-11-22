package com.ustsinau.springsecuritykeycloakapi.exception;

public class UserEmailOrPasswordException extends ApiException{
    public UserEmailOrPasswordException(String message, String errorCode) {

        super(message, errorCode);
    }
}
