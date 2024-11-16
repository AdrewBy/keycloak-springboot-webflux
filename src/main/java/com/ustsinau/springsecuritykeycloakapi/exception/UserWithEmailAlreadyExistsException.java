package com.ustsinau.springsecuritykeycloakapi.exception;

public class UserWithEmailAlreadyExistsException extends ApiException{
    public UserWithEmailAlreadyExistsException(String message, String errorCode) {
        super(message, errorCode);
    }
}
