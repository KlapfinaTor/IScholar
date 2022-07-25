package at.fhj.ischolar.data;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "ROOM_NAME")
    private String name;


    public Room(String name) {
        this.name = name;
    }
    public Room(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Room() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    //This is used by the choicebox
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return id == room.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
