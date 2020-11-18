package com.battlesnake.starter.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class Grid {
    private List<Cell[]> grid = new ArrayList<Cell[]>();
    private int width;
    private int height;
    private String youId;

    public void initializeGrid(int width, int height) {
        this.width = width;
        this.height = height;
        for(int x = 0; x < width; x++) {
            grid.add(new Cell[height]);
            for(int y = 0; y < height; y++) {
                grid.get(x)[y] = new Cell(x, y, true);
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
                        .setOccupyingSnakeHead(snake.get("id").asText())
                        .setFreeCell(false);
                snake.get("body").forEach(coordinate -> {
                    grid.get(coordinate.get("x").asInt())[coordinate.get("y").asInt()]
                            .setOccupyingSnake(snake.get("id").asText())
                            .setFreeCell(false);
                });
            });
        }
    }

    private void setYouSnakeCells(JsonNode you) {
        if(you != null) {
            grid.get(you.get("head").get("x").asInt())[you.get("head").get("y").asInt()]
                    .setOccupyingSnake(you.get("id").asText())
                    .setOccupyingSnakeHead(you.get("id").asText())
                    .setFreeCell(false);
            you.get("body").forEach(coordinate -> {
                grid.get(coordinate.get("x").asInt())[coordinate.get("y").asInt()]
                        .setOccupyingSnake(you.get("id").asText())
                        .setFreeCell(false);
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

    //private
    public Coordinate findNearestFoodCoordinate() {
        Coordinate head = this.getYouHead();
        List<Coordinate> foodCells = new ArrayList<Coordinate>();
        grid.forEach(list -> {
            Arrays.stream(list).filter(cell -> cell.getHasFood() == true)
                    .collect(Collectors.toList())
                    .forEach(cell -> {
                        foodCells.add(cell.getCoordinate());
                    });
        });

        long distance = 1000000;
        Coordinate min = new Coordinate(-1, -1);
        for(int i = 0; i < foodCells.size(); i++){
            if(head.distanceToCoordinate(foodCells.get(i)) < distance) {
                min = foodCells.get(i);
            }
        }
        return min;
    }

    public List<Coordinate> pathToNearestFood() {
        return findShortestPath(this.getYouHead(), this.findNearestFoodCoordinate());
    }

    //private
    public List<Coordinate> findShortestPath(Coordinate start, Coordinate end) {
        List<String> path = new ArrayList<String >();

        List<Coordinate[]> previous = new ArrayList<Coordinate[]>();
        List<Boolean[]> visited = new ArrayList<Boolean[]>();
        for(int x = 0; x < this.getWidth(); x++) {
            previous.add(new Coordinate[this.getHeight()]);
            visited.add(new Boolean[this.getHeight()]);
            for(int y = 0; y < this.getHeight(); y++) {
                previous.get(x)[y] = null;
                visited.get(x)[y] = false;
            }
        }

        Queue<Coordinate> bfsQueue = new LinkedList<Coordinate>();
        bfsQueue.add(start);
        visited.get(start.getX())[start.getY()] = true;

        while(!bfsQueue.isEmpty()) {
            Coordinate node = bfsQueue.remove();
            if(node.equals(end)) {
                bfsQueue.clear();
                break;
            }
            List<Coordinate> neighbours = getNeighbours(node);
            for(Coordinate neighbour: neighbours) {
                if(!visited.get(neighbour.getX())[neighbour.getY()]) {
                    bfsQueue.add(neighbour);
                    visited.get(neighbour.getX())[neighbour.getY()] = true;
                    previous.get(neighbour.getX())[neighbour.getY()] = node;
                }
            }
        }

        return reconstructPath(previous, start, end);
    }

    public List<Coordinate> getNeighbours(Coordinate cell) {
        List<Coordinate> neighbours = new ArrayList<Coordinate>();
        if(cell.getX() - 1 > 0 && grid.get(cell.getX() - 1)[cell.getY()].getFreeCell()) {
            neighbours.add(new Coordinate(cell.getX() - 1, cell.getY()));
        }
        if(cell.getX() + 1 < this.getWidth() && grid.get(cell.getX() + 1)[cell.getY()].getFreeCell()) {
            neighbours.add(new Coordinate(cell.getX() + 1, cell.getY()));
        }
        if(cell.getY() - 1 > 0 && grid.get(cell.getX())[cell.getY() - 1].getFreeCell()) {
            neighbours.add(new Coordinate(cell.getX(), cell.getY() - 1));
        }
        if(cell.getY() + 1 < this.getHeight() && grid.get(cell.getX())[cell.getY() + 1].getFreeCell()) {
            neighbours.add(new Coordinate(cell.getX(), cell.getY() + 1));
        }
        return neighbours;
    }

    //private
    public List<Coordinate> reconstructPath(List<Coordinate[]> previous, Coordinate start, Coordinate end) {
        List<Coordinate> path = new ArrayList<Coordinate>();
        if(previous != null) {
            Coordinate prev = previous.get(end.getX())[end.getY()];
            while (prev != null && !prev.equals(start)) {
                path.add(prev);
                prev = previous.get(prev.getX())[prev.getY()];
            }
        }
        Collections.reverse(path);
        return path;
    }

}
