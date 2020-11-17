package com.battlesnake.starter.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Coordinate {
    private int x;
    private int y;


    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(JsonNode node) {
        if(node != null) {
            this.x = node.get("x") != null ? node.get("x").asInt() : null;
            this.y = node.get("y") != null ? node.get("y").asInt() : null;
        }
    }

    public void setCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Boolean equals(Coordinate coordinate) {
        return this.x == coordinate.getX() && this.y == coordinate.getY();
    }

    public Boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }

    public long distanceToCoordinate(Coordinate endpoint) {
        return ((endpoint.getX() - this.getX())*(endpoint.getX() - this.getX())
                        + (endpoint.getY() - this.getY())* (endpoint.getY() - this.getY()) );
    }
}
