package com.example.identifyService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1000, "You do not have permission", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    WRONG_PASSWORD(1002,"Password is wrong" ,HttpStatus.BAD_REQUEST ),
    UNAUTHENTICATED(1003, "Unauthenticated" , HttpStatus.UNAUTHORIZED ),
    PASSWORD_MINIMUM(1004, "Password minimum 8 characters" , HttpStatus.BAD_REQUEST ),
    INVALID_PASSWORD(1005,"Invalid password" ,HttpStatus.BAD_REQUEST ),
    NOT_ACTIVATE_YET(1006,"User not activate yet" ,HttpStatus.BAD_REQUEST ),
    EXPIRED_TOKEN(1007,"Expired token" ,HttpStatus.BAD_REQUEST ),
    ALREADY_ACTIVATE(1008,"User already activate" ,HttpStatus.BAD_REQUEST ),;

    private final Integer code;
    private final String message;
    private final HttpStatusCode status;

    ErrorCode(Integer code, String message,HttpStatusCode status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}