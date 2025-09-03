package com.example.identifyService.validator;

import jakarta.validation.ConstraintValidator;

public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    @Override
    public boolean isValid(String password, jakarta.validation.ConstraintValidatorContext constraintValidatorContext) {
        if(password == null) {
            return false;
        }
        return password.matches( PASSWORD_PATTERN);
    }
}
