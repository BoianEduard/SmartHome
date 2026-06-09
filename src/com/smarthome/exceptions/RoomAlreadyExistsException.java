package com.smarthome.exceptions;

public class RoomAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1L;

    public RoomAlreadyExistsException(String message) {
        super(message);
    }
}
