package com.battlesnake.starter.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.ArrayList;

@Data
@Accessors(chain = true)
public class Grid {
    List<Cell[]> grid = new ArrayList<Cell[]>();

    public void initializeGrid(int width, int height) {
        for(int x = 0; x < width; x++) {
            grid.add(new Cell[height]);
            for(int y = 0; y < height; y++) {
                grid.get(x)[y] = new Cell(x, y, false);
            }
        }
    }

    public void setGameState(JsonNode node) {
        if(node != null) {
            setFoodCells(node.get("food"));
            setEnemySnakeCells(node.get("snakes"));
        }
    }

    private void setFoodCells(JsonNode food) {
        if(food != null) {
            food.forEach(coordinate -> {
                grid.get(coordinate.get("x").asInt())[coordinate.get("y").asInt()].setHasFood(true);
            });
        }
    }

    private void setEnemySnakeCells(JsonNode snakes) {
        if(snakes != null) {
            snakes.forEach(snake -> {
                grid.get(snake.get("head").get("x").asInt())[snake.get("head").get("y").asInt()]
                        .setOccupyingSnake(snake.get("id").asText())
                        .setOccupyingSnakeHead(snake.get("id").asText());
                snake.get("body").forEach(coordinate -> {
                    grid.get(coordinate.get("x").asInt())[coordinate.get("y").asInt()]
                            .setOccupyingSnake(snake.get("id").asText());
                });
            });
        }
    }
}
