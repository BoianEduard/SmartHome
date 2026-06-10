package com.smarthome.tests;

import com.smarthome.domain.Room;
import com.smarthome.domain.SmartHome;
import com.smarthome.domain.SmokeSensor;
import com.smarthome.domain.TemperatureSensor;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SmartHomeTest {

    private SmartHome home;
    private Room living;
    private Room kitchen;

    @Before
    public void setUp() {
        home    = new SmartHome("Test Home");
        living  = new Room("R1", "Living Room");
        kitchen = new Room("R2", "Kitchen");

        living.addSensor(new TemperatureSensor("T1", "Temp", 22.0));
        kitchen.addSensor(new SmokeSensor("S1", "Smoke", 1.0));
    }

    @Test
    public void testSmartHomeCreation() {
        assertEquals("Test Home", home.getName());
        assertTrue(home.getRooms().isEmpty());
    }

    @Test
    public void testAddRoom() throws RoomAlreadyExistsException {
        home.addRoom(living);
        assertEquals(1, home.getRooms().size());
    }

    @Test
    public void testAddMultipleRooms() throws RoomAlreadyExistsException {
        home.addRoom(living);
        home.addRoom(kitchen);
        assertEquals(2, home.getRooms().size());
    }

    @Test(expected = RoomAlreadyExistsException.class)
    public void testAddDuplicateRoom() throws RoomAlreadyExistsException {
        home.addRoom(living);
        home.addRoom(living);
    }

    @Test
    public void testListenersFireOnRoomAdd() throws RoomAlreadyExistsException {
        home.addRoom(living);
        assertEquals(1, home.getLoggedRooms());
        assertEquals(1, home.getEmailedRooms());
        assertEquals(1, home.getDashboardRooms());
    }

    @Test
    public void testListenerCountScalesWithRooms() throws RoomAlreadyExistsException {
        home.addRoom(living);
        home.addRoom(kitchen);
        assertEquals(2, home.getLoggedRooms());
        assertEquals(2, home.getEmailedRooms());
        assertEquals(2, home.getDashboardRooms());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRoomListIsUnmodifiable() throws RoomAlreadyExistsException {
        home.addRoom(living);
        home.getRooms().add(kitchen);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmartHomeBlankName() {
        new SmartHome("   ");
    }
}
