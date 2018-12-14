package Chatbot;

import bot.Chatbot;
import bot.modules.Quotes;
import bot.utils.CONSTANTS;
import bot.utils.Database;
import bot.utils.Message;
import bot.utils.exceptions.MissingConfigurationsException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;

public class InitScript {
    final static String ROOT = "src/test/java/Chatbot/";

    public static void main(String[] args) throws SQLException, IOException {
        HashMap<String, String> config = Chatbot.createConfig(args);
        Database db = null;
        try {
            db = new Database(config);
        } catch (MissingConfigurationsException e) {
            e.printStackTrace();
            System.exit(1);
        }

        BufferedReader resetFile = new BufferedReader(new InputStreamReader(InitScript.class.getClassLoader().getResourceAsStream("MySQL/Reset.sql")));
        StringBuilder execString = new StringBuilder();
        String line;
        while ((line = resetFile.readLine()) != null) {
            execString.append(line).append("\n");
        }
        resetFile.close();
        db.runSQL(execString.toString());
        System.out.println("Database reset");

        Connection connection = db.getConnection();

        PreparedStatement createImage_STMT = connection.prepareStatement("INSERT INTO Images (url) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        PreparedStatement createSubreddit_STMT = connection.prepareStatement("INSERT INTO Subreddits (type, link) VALUES (?,?)");

        //Create dogbot human
        db.saveHuman("Rex Atkinson");
        db.saveHuman("John Smith");

        //region Boot
        try {
            //Responses
            PreparedStatement response_STMT = connection.prepareStatement("INSERT INTO BootResponses (message) VALUES (?)");
            for (String response : Files.readAllLines(Paths.get(ROOT + "Boot/responses.txt"))) {
                response_STMT.setString(1, response);
                response_STMT.addBatch();
            }
            response_STMT.executeBatch();

            //Images
            PreparedStatement bootImage_STMT = connection.prepareStatement("INSERT INTO BootImages (image_id) VALUES (?)");
            for (String image : Files.readAllLines(Paths.get(ROOT + "Boot/images.txt"))) {
                createImage_STMT.setString(1, image);
                createImage_STMT.execute();
                ResultSet keySet = createImage_STMT.getGeneratedKeys();
                if (keySet.next()) {
                    bootImage_STMT.setInt(1, keySet.getInt(1));
                    bootImage_STMT.addBatch();
                }
            }
            bootImage_STMT.executeBatch();
            System.out.println("Boot created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Boot failure");
        }
        //endregion

        //region ExtraGoodDogs
        try {
            //Images
            PreparedStatement extraGoodDog_STMT = connection.prepareStatement("INSERT INTO ExtraGoodDogs (image_id) VALUES (?)");
            for (String image : Files.readAllLines(Paths.get(ROOT + "Dogs/extraGoodDogs.txt"))) {
                createImage_STMT.setString(1, image);
                createImage_STMT.execute();
                ResultSet keySet = createImage_STMT.getGeneratedKeys();
                if (keySet.next()) {
                    extraGoodDog_STMT.setInt(1, keySet.getInt(1));
                    extraGoodDog_STMT.addBatch();
                }
            }
            extraGoodDog_STMT.executeBatch();
            System.out.println("Extra good dogs created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("There are no special barks");
        }
        //endregion

        //region 8Ball
        try {
            //Responses
            PreparedStatement response_STMT = connection.prepareStatement("INSERT INTO EightBallResponses (message) VALUES (?)");
            for (String response : Files.readAllLines(Paths.get(ROOT + "EightBall/responses.txt"))) {
                response_STMT.setString(1, response);
                response_STMT.addBatch();
            }
            response_STMT.executeBatch();
            System.out.println("8Ball created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ball is out");
        }
        //endregion

        //region Reacts
        try {
            //Images
            PreparedStatement react_STMT = connection.prepareStatement("INSERT INTO Reacts (image_id) VALUES (?)");
            for (String image : Files.readAllLines(Paths.get(ROOT + "Reacts/catReacts.txt"))) {
                createImage_STMT.setString(1, image);
                createImage_STMT.execute();
                ResultSet keySet = createImage_STMT.getGeneratedKeys();
                if (keySet.next()) {
                    react_STMT.setInt(1, keySet.getInt(1));
                    react_STMT.addBatch();
                }
            }
            react_STMT.executeBatch();
            System.out.println("Reacts created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("The reaction was NULL");
        }
        //endregion

        //region Reddit

        //region Birds
        try {
            //Responses
            PreparedStatement response_STMT = connection.prepareStatement("INSERT INTO BirdResponses (message) VALUES (?)");
            for (String response : Files.readAllLines(Paths.get(ROOT + "Birds/responses.txt"))) {
                response_STMT.setString(1, response);
                response_STMT.addBatch();
            }
            response_STMT.executeBatch();

            //Subreddits
            createSubreddit_STMT.setString(1, "Birds");
            for (String subreddit : Files.readAllLines(Paths.get(ROOT + "Birds/subreddits.txt"))) {
                createSubreddit_STMT.setString(2, subreddit);
                createSubreddit_STMT.addBatch();
            }
            createSubreddit_STMT.executeBatch();
            System.out.println("Birds created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Birds failed to fly");
        }
        //endregion

        //region Cats
        try {
            //Responses
            PreparedStatement response_STMT = connection.prepareStatement("INSERT INTO CatResponses (message) VALUES (?)");
            for (String response : Files.readAllLines(Paths.get(ROOT + "Cats/responses.txt"))) {
                response_STMT.setString(1, response);
                response_STMT.addBatch();
            }
            response_STMT.executeBatch();

            //Subreddits
            createSubreddit_STMT.setString(1, "Cats");
            for (String subreddit : Files.readAllLines(Paths.get(ROOT + "Cats/subreddits.txt"))) {
                createSubreddit_STMT.setString(2, subreddit);
                createSubreddit_STMT.addBatch();
            }
            createSubreddit_STMT.executeBatch();
            System.out.println("Cats created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cats lost its 9 lives");
        }
        //endregion

        //region Dogs
        try {
            //Responses
            PreparedStatement response_STMT = connection.prepareStatement("INSERT INTO DogResponses (message) VALUES (?)");
            for (String response : Files.readAllLines(Paths.get(ROOT + "Dogs/responses.txt"))) {
                response_STMT.setString(1, response);
                response_STMT.addBatch();
            }
            response_STMT.executeBatch();

            //Subreddits
            createSubreddit_STMT.setString(1, "Dogs");
            for (String subreddit : Files.readAllLines(Paths.get(ROOT + "Dogs/subreddits.txt"))) {
                createSubreddit_STMT.setString(2, subreddit);
                createSubreddit_STMT.addBatch();
            }
            createSubreddit_STMT.executeBatch();
            System.out.println("Dogs created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Doggo didn't bark");
        }
        //endregion

        //endregion

        //region Quotes
        Quotes quote_module = new Quotes(db);
        quote_module.prepareStatements(connection);
        try {
            JSONParser jsonParser = new JSONParser();
            //Quotes from 16/12/2018
            String threadName = "1388627754518614";
            int threadId = db.getThreadIdFromName(threadName);
            FileReader fileReader = new FileReader(ROOT + "Quotes/" + threadName + "-quotes.json");
            JSONArray quotes = (JSONArray) jsonParser.parse(fileReader);
            fileReader.close();
            for (Object q : quotes) {
                JSONObject quote = (JSONObject) q;
                String senderName = (String) ((JSONObject) quote.get("sender")).get("name");
                String message = (String) (quote.get("message"));
                String imageUrl = "";
                LocalDate date = LocalDate.parse((String) quote.get("timestamp"), CONSTANTS.DATE_FORMATTER);
                Message m1 = Message.fromResultSet(db.saveMessage(senderName, threadId, message, imageUrl, date));
                quote_module.saveQuote(threadId, m1);
                System.out.println(m1);
            }
            System.out.println("Quotes created");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("You are un-quotable");
        }
        //endregion
    }
}
