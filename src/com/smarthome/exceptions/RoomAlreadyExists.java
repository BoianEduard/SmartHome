package com.smarthome.exceptions;

import com.smarthome.domain.Room;

public class RoomAlreadyExists extends Exception {

    private static final long serialVersionUID = 1L;

    public RoomAlreadyExists(String message) {
        super(message);
    }
}
