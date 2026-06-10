package com.smarthome.domain;

import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.utils.RoomRegistrationListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmartHome {

    private final String name;
    private final List<Room> rooms = new ArrayList<>();
    private final List<RoomRegistrationListener> listeners = new ArrayList<>();
    private int loggedRooms;
    private int emailedRooms;
    private int dashboardRooms;

    public SmartHome(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Please provide a name for the Smart-Home system");
        }

        this.name = name;
        listeners.add(new ConsoleLogListener());
        listeners.add(new EmailAlertListener());
        listeners.add(new DashboardListener());
    }

    public void addRoom(Room room) throws RoomAlreadyExistsException {
        if (rooms.stream().anyMatch(r -> r.getId().equals(room.getId()))) {
            throw new RoomAlreadyExistsException("Room with id '" + room.getId() + "' already exists");
        }

        rooms.add(room);
        notifyListeners(room);
    }

    private void notifyListeners(Room room) {
        for (RoomRegistrationListener listener : listeners) {
            listener.onRoomAdded(room);
        }
    }

    public String getName() {
        return name;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public int getLoggedRooms() {
        return loggedRooms;
    }

    public void setLoggedRooms(int loggedRooms) {
        this.loggedRooms = loggedRooms;
    }

    public List<RoomRegistrationListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public int getDashboardRooms() {
        return dashboardRooms;
    }

    public void setDashboardRooms(int dashboardRooms) {
        this.dashboardRooms = dashboardRooms;
    }

    public int getEmailedRooms() {
        return emailedRooms;
    }

    public void setEmailedRooms(int emailedRooms) {
        this.emailedRooms = emailedRooms;
    }

    class ConsoleLogListener implements RoomRegistrationListener {
        @Override
        public void onRoomAdded(Room room) {
            System.out.println("[LOG] Room registered: " + room.getName());
            loggedRooms++;
        }
    }

    class EmailAlertListener implements RoomRegistrationListener {
        @Override
        public void onRoomAdded(Room room) {
            System.out.println("[EMAIL] Notification sent for new room: " + room.getName());
            emailedRooms++;
        }
    }

    class DashboardListener implements RoomRegistrationListener {
        private static final DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public void onRoomAdded(Room room) {
            System.out.println("[DASHBOARD] Room '" + room.getName()
                    + "' added at " + LocalDateTime.now().format(FMT));
            dashboardRooms++;
        }
    }
}
