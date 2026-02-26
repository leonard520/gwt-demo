package com.example.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Simple model class that represents a generic item.
 * <p>
 * Rewritten from: com.example.client.model.Item (GWT IsSerializable)
 * to a plain Java bean with LocalDate (replacing java.util.Date).
 */
public class Item {

    private long id;

    private String name;

    private String description;

    private LocalDate date = LocalDate.now();

    /**
     * Must have a default no-arg constructor (for Jackson deserialization).
     */
    public Item() {
        super();
    }

    /**
     * Constructor.
     *
     * @param name        the item name
     * @param description the item description
     * @param date        the item date
     */
    public Item(String name, String description, LocalDate date) {
        this.name = name;
        this.description = description;
        this.date = date;
    }

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final LocalDate getDate() {
        return date;
    }

    public final void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Item other = (Item) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Item [date=" + date + ", description=" + description + ", id="
                + id + ", name=" + name + "]";
    }
}
