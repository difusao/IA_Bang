package com.ag;

public class Shot {
    private int id;
    private Double space;
    private Double value;

    public Shot(int id, Double space, Double value) {
        this.id = id;
        this.space = space;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getSpace() {
        return space;
    }

    public void setSpace(Double space) {
        this.space = space;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
