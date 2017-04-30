package com.company;


import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class Main{

    static final String URL = "jdbc:sqlserver://localhost:1433;DatabaseName=ECYatzy";
    static Connection connection;
    static ResultSet rs;



    public static void main(String[] args) {
        Scanner input=new Scanner(System.in);
        int choice, seeGame;
        String playername;
        try {

            System.out.println("Choose from menu:");
                    //System.out.println("1: Play a game");
                    System.out.println("2. View highscores for a game,final scores for all players");
                    System.out.println("3: See player history for all games played");
                    System.out.println("4: See game results including rounds for a specific game");
                    System.out.println("5: See category combos:");

            choice=Integer.parseInt(input.nextLine());

            switch (choice){
                case 1:
                        Playgame playgame=new Playgame();
                        playgame.run();
                        break;
                case 2:
                        viewHighscores();
                        break;
                case 3:
                        System.out.println("Available players");
                        connect();
                        rs=connection.prepareStatement("SELECT * FROM Players").executeQuery();
                        DBTablePrinter.printResultSet(rs,15);
                        connection.close();
                        System.out.println("Who's history would you like to look at, name please:");
                        playername=input.nextLine();
                        viewPlayerHistory(playername);
                        break;
                case 4:
                        System.out.println("What game would you like to look at:");
                        seeGame=Integer.parseInt(input.nextLine());
                        viewTurnsForGame(seeGame);
                        break;
                case 5: viewScoreTypes();
                        break;


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }catch(NumberFormatException e) {
            System.out.println("Invalid input try again, this time a number thank you");
            return;
        }
    }

    static void connect() throws SQLException {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        connection = DriverManager.getConnection(URL, "test", "test");

    }

    static void viewScoreTypes() throws SQLException{
        connect();
        int roundID=0;
        System.out.println("View all scoretypes avaliable");
        rs=connection.prepareStatement("SELECT * FROM ScoreTypes").executeQuery();
        DBTablePrinter.printResultSet(rs,15);


        System.out.println(" -------------------------------- ");

        connection.close();
    }


    static void viewHighscores() throws SQLException {
        connect();

        System.out.println("Viewing highscores...");

        rs = connection.prepareStatement("SELECT Score, Name FROM PlayersInGame "
                + "INNER JOIN Players ON Player_ID = PlayerID ORDER BY Score DESC").executeQuery();
        DBTablePrinter.printResultSet(rs);
        System.out.println(" -------------------------------- ");

        connection.close();
    }

    static void viewPlayerHistory(String player) throws SQLException {

        int playerID;
        int[] gameIDs;

        connect();

        System.out.println("Viewing games for player '" + player + "'...");

        rs = connection.prepareStatement("SELECT PlayerID FROM Players WHERE Name = N'" + player + "'")
                .executeQuery();
        if (!rs.next()){
            System.out.println("No games found for '" + player + "'.");
            return;
        }
        playerID = rs.getInt(1);

        rs = connection.prepareStatement("SELECT COUNT(*) FROM PlayersInGame WHERE Player_ID = " + playerID)
                .executeQuery();
        rs.next();
        gameIDs = new int[rs.getInt(1)];

        rs = connection.prepareStatement(
                "SELECT Game_ID FROM PlayersInGame WHERE Player_ID = " + playerID)
                .executeQuery();
        int i = 0;
        while (rs.next()) {
            gameIDs[i] = rs.getInt(1);
            i++;
        }

        for (int j : gameIDs) {
            rs = connection.prepareStatement("SELECT Date FROM [dbo].[Date] WHERE GameID = " + j).executeQuery();
            rs.next();
            System.out.println("GameID:   End date:");
            System.out.println(j + "         " + rs.getDate(1));
            System.out.println();

            rs = connection
                    .prepareStatement("SELECT Score, Name FROM PlayersInGame INNER JOIN Players "
                            + "ON Player_ID = PlayerID WHERE Game_ID = " + j + " ORDER BY Score DESC")
                    .executeQuery();
            rs.next();
            System.out.println("Score:    Winner:");
            System.out.println(rs.getInt(1) + "       " + rs.getString(2));
            System.out.println();
            System.out.println("Score:    Loser:");
            while (rs.next()) {
                System.out.println(rs.getInt(1) + "       " + rs.getString(2));
            }
            System.out.println(" -------------------------------- ");
        }

        connection.close();

    }

    static void viewTurnsForGame(int gameID) throws SQLException {

        connect();

        System.out.println("Viewing turns for game " + gameID + "...");
        System.out.println("Turn:  Player:              Score type:       Score: ");

        rs = connection.prepareStatement("SELECT RoundID,RoundNr, p.Name, s.Name, Score FROM Round "
                + "INNER JOIN Players AS p ON Player_ID = PlayerID INNER JOIN ScoreTypes AS s "
                + "ON ScoreType_ID = ScoreTypeID WHERE Game_ID = " + gameID)
                .executeQuery();

       // DBTablePrinter.printResultSet(rs);
       while (rs.next()){
            System.out.printf("%-6d %-20s %-17s %-7d %n", rs.getInt(2), rs.getString(3).trim(), rs.getString(4).trim(), rs.getInt(5));

            int turnID = rs.getInt(1);
            ResultSet dice = connection.prepareStatement("SELECT Nr FROM Dice WHERE Round_ID = " + turnID)
                    .executeQuery();
            System.out.print("Dice:  ");
            while (dice.next()){
                System.out.print( dice.getInt(1) + " ");
            }
            System.out.println();
        }
        System.out.println(" -------------------------------- ");

        connection.close();

    }

}

