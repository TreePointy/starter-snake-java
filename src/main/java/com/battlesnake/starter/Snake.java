package com.battlesnake.starter;

import com.battlesnake.starter.api.Coordinate;
import com.battlesnake.starter.api.Grid;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.*;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

/**
 * This is a simple Battlesnake server written in Java.
 * 
 * For instructions see
 * https://github.com/BattlesnakeOfficial/starter-snake-java/README.md
 */
public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    /**
     * Main entry point.
     *
     * @param args are ignored.
     */
    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port != null) {
            LOG.info("Found system provided port: {}", port);
        } else {
            LOG.info("Using default port: {}", port);
            port = "8080";
        }
        port(Integer.parseInt(port));
        get("/",  HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    /**
     * Handler class for dealing with the routes set up in the main method.
     */
    public static class Handler {

        /**
         * For the start/end request
         */
        private static final Map<String, String> EMPTY = new HashMap<>();

        /**
         * Generic processor that prints out the request and response from the methods.
         *
         * @param req
         * @param res
         * @return
         */
        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                //LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/")) {
                    snakeResponse = index();
                } else if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }
                //LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));
                return snakeResponse;
            } catch (Exception e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

    
        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @return a response back to the engine containing the Battlesnake setup
         *         values.
         */
        public Map<String, String> index() {         
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", "Spaghetti, The Spanning Snake");  // TODO: Your Battlesnake Username
            response.put("color", "#888888");     // TODO: Personalize
            response.put("head", "default");  // TODO: Personalize
            response.put("tail", "default");  // TODO: Personalize
            return response;
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @param startRequest a JSON data map containing the information about the game
         *                     that is about to be played.
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> start(JsonNode startRequest) {
            LOG.info("START");
            return EMPTY;
        }

        /**
         * This method is called on every turn of a game. It's how your snake decides
         * where to move.
         * 
         * Valid moves are "up", "down", "left", or "right".
         *
         * @param moveRequest a map containing the JSON sent to this snake. Use this
         *                    data to decide your next move.
         * @return a response back to the engine containing Battlesnake movement values.
         */
        public Map<String, String> move(JsonNode moveRequest) {
//            try {
//                LOG.info("Data: {}", JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(moveRequest));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }

            //initialize the grid
            Grid grid = new Grid();
            grid.initializeGrid(moveRequest.get("board").get("width").asInt(),
                    moveRequest.get("board").get("height").asInt());

            grid.setGameState(moveRequest);

            HashMap<Coordinate, String> possibleMoves = findPossibleMove(grid);
//            LOG.info("POSSIBLE MOVES: {}", possibleMoves);

//            // Choose a random direction to move in
//            int choice = new Random().nextInt(possibleMoves.size());

            List<Coordinate> pathToNearestFood = grid.pathToNearestFood();
            String move = possibleMoves.get(pathToNearestFood.get(0));


            LOG.info("MOVE {}", move);

            Map<String, String> response = new HashMap<>();
            response.put("move", move);
            return response;
        }

        /**
         * This method is called when a game your Battlesnake was in ends.
         * 
         * It is purely for informational purposes, you don't have to make any decisions
         * here.
         *
         * @param endRequest a map containing the JSON sent to this snake. Use this data
         *                   to know which game has ended
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> end(JsonNode endRequest) {

            LOG.info("END");
            return EMPTY;
        }

        //primitive to test moving
        HashMap<Coordinate, String> findPossibleMove(Grid grid) {
            //List<String> possibleMoves = new ArrayList<>();
            Coordinate head = grid.getYouHead();
            HashMap<Coordinate, String> possibleMoves = new HashMap<Coordinate, String>();

            if(head.getX() > 0
                    && !selfOverlap(grid.getFullYouSnake(), new Coordinate(head.getX() - 1, head.getY()))) {
                possibleMoves.put(new Coordinate(head.getX() - 1, head.getY()), "left");
            }
            if(head.getX() + 1 < grid.getWidth()
                    && !selfOverlap(grid.getFullYouSnake(), new Coordinate(head.getX() + 1, head.getY()))) {
                possibleMoves.put(new Coordinate(head.getX() + 1, head.getY()), "right");
            }
            if(head.getY() > 0
                    && !selfOverlap(grid.getFullYouSnake(), new Coordinate(head.getX(), head.getY() - 1))) {
                possibleMoves.put(new Coordinate(head.getX(), head.getY() - 1), "down");
            }
            if(head.getY() + 1 < grid.getHeight()
                    && !selfOverlap(grid.getFullYouSnake(), new Coordinate(head.getX(), head.getY() + 1))) {
                possibleMoves.put(new Coordinate(head.getX(), head.getY() + 1), "up");
            }

            return possibleMoves;
        }

        Boolean selfOverlap(List<Coordinate> bodyPositions, Coordinate coordinate) {

            for(int i = 0; i < bodyPositions.size(); i++) {
                if(bodyPositions.get(i).equals(coordinate)) {
                    return true;
                }
            }

            return false;
        }
    }


}
