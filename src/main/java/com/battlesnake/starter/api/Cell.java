package com.battlesnake.starter.api;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Cell {
    private Coordinate coordinate;
    private Boolean freeCell;
    private Boolean hasFood;
    private String occupyingSnakeHead;
    private String occupyingSnake;
    private int snakeBodyOrderId;

    public Cell(int x, int y, Boolean freeCell, int order) {
        this.coordinate = new Coordinate(x, y);
        this.freeCell = freeCell;
        this.hasFood = false;
        this.occupyingSnake = "";
        this.occupyingSnakeHead = "";
        this.snakeBodyOrderId = order;
    }
}
