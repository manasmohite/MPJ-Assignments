package model;

/**
 * Represents a bookable campus facility (room, lab, hall, etc.)
 */
public class Facility {
    private int facilityId;
    private String name;
    private String building;
    private int floor;
    private String roomNumber;
    private String type;       // CLASSROOM, LAB, SEMINAR_HALL, AUDITORIUM, SPORTS
    private int capacity;
    private String description;
    private boolean isActive;

    public Facility(int facilityId, String name, String building,
                    int floor, String roomNumber, String type,
                    int capacity, String description, boolean isActive) {
        this.facilityId = facilityId;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters & Setters
    public int getFacilityId() { return facilityId; }
    public void setFacilityId(int facilityId) { this.facilityId = facilityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getLocationString() {
        return building + " | Floor " + floor + " | Room " + roomNumber;
    }

    @Override
    public String toString() {
        return name + " (" + type + ") — " + getLocationString() + " | Capacity: " + capacity;
    }
}
