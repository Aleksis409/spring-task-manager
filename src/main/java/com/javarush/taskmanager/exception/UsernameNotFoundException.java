package com.javarush.taskmanager.exception;

public class UsernameNotFoundException extends BusinessException {
    public UsernameNotFoundException(String message) {
        super(message);
    }
}
