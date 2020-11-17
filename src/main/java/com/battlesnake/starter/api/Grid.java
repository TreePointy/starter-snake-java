package com.battlesnake.starter.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class Grid {
    List<Cell[]> grid = new ArrayList<Cell[]>();
    String youId;

    public void initializeGrid(int width, int height) {
        for(int x = 0; x < width; x++) {
            grid.add(new Cell[height]);
            for(int y = 0; y < height; y++) {
                grid.get(x)[y] = new Cell(x, y, false);
            }
        }
    }

    public void setGameState(JsonNode node) {
        youId = node.get("you").get("id").asText();
        if(node != null) {
            setFoodCells(node.get("board").get("food"));
            setEnemySnakeCells(node.get("board").get("snakes"));
            setYouSnakeCells(node.get("you"));
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

    private void setYouSnakeCells(JsonNode you) {
        if(you != null) {
            grid.get(you.get("head").get("x").asInt())[you.get("head").get("y").asInt()]
                    .setOccupyingSnake(you.get("id").asText())
                    .setOccupyingSnakeHead(you.get("id").asText());
            you.get("body").forEach(coordinate -> {
                grid.get(coordinate.get("x").asInt())[coordinate.get("y").asInt()]
                        .setOccupyingSnake(you.get("id").asText());
            });
        }
    }

    public List<Coordinate> getFullYouSnake() {
        if(youId != null) {
            List<Coordinate> fullYouSnake = new ArrayList<Coordinate>();
            grid.forEach(list -> {
                Arrays.stream(list).filter(cell -> cell.getOccupyingSnake() == youId)
                        .collect(Collectors.toList())
                        .forEach(cell -> {
                            fullYouSnake.add(cell.getCoordinate());
                        });
            });
            return fullYouSnake;
        }

        return null;
    }

    public Coordinate getYouHead() {
        if(youId != null) {
            List<Coordinate> head = new ArrayList<Coordinate>();
            grid.forEach(list -> {
                Arrays.stream(list).filter(cell -> cell.getOccupyingSnakeHead() == youId)
                        .collect(Collectors.toList())
                        .forEach(cell -> {
                            head.add(cell.getCoordinate());
                        });
            });
            if(head.size() == 1) {
                return head.get(0);
            } else {
                return null;
            }
        }
        return null;
    }
}
