package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

public class PlayerManagement{

    private static Player humanPlayer;
    private static Player AiPlayer;

    public PlayerManagement(GameState gameState,ActorRef out){
        initializePlayer1(gameState,out);
        initializePlayer2(gameState,out);
    }

    public static void setPlayer1Mana(ActorRef out, int mana){
        for (int m = 0; m<=mana; m++) {
            humanPlayer.setMana(m);
            BasicCommands.setPlayer1Mana(out, humanPlayer);
        }
    }

    public static void setPlayer2Mana(ActorRef out, int mana){
        for (int m = 0; m<=mana; m++) {
            AiPlayer.setMana(m);
            BasicCommands.setPlayer2Mana(out, AiPlayer);
        }
    }

    public static void initializePlayer1(GameState gameState,ActorRef out){
        BasicCommands.addPlayer1Notification(out, "drawHumanPlayer", 2);

        //create player+set player's health
        humanPlayer=new Player(20,0,"HumanPlayer");
        BasicCommands.setPlayer1Health(out,humanPlayer);

        // create humanPlayer's unit and draw it onto the board
        Unit unit_humanPlayer = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, gameState.unitId, Unit.class);
        gameState.unitId++;
        Tile tile_humanPlayer=BasicObjectBuilders.loadTile(1, 2);
        unit_humanPlayer.setPositionByTile(tile_humanPlayer);
        unit_humanPlayer.setHealth(20);
        BasicCommands.drawUnit(out, unit_humanPlayer, tile_humanPlayer);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        //set the health of the unit
        BasicCommands.setUnitHealth(out,unit_humanPlayer,20);

        //store in the gameState
        gameState.humanPlayer=unit_humanPlayer;
        gameState.humanPlayerUnitList.add(unit_humanPlayer);
    }

    public static void initializePlayer2(GameState gameState,ActorRef out){
        BasicCommands.addPlayer1Notification(out, "drawAiPlayer", 2);

        AiPlayer=new Player(20,0,"AiPlayer");
        BasicCommands.setPlayer2Health(out,AiPlayer);

        Unit unit_AiPlayer = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, gameState.unitId, Unit.class);
        gameState.unitId++;
        Tile tile_AiPlayer=BasicObjectBuilders.loadTile(7, 2);
        unit_AiPlayer.setPositionByTile(tile_AiPlayer);
        unit_AiPlayer.setHealth(20);
        BasicCommands.drawUnit(out, unit_AiPlayer, tile_AiPlayer);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

        BasicCommands.setUnitHealth(out,unit_AiPlayer,20);
        gameState.aiPlayer=unit_AiPlayer;
        gameState.aiPlayerUnitList.add(unit_AiPlayer);
    }

    public static Player getHumanPlayer(){
        return humanPlayer;
    }

    public static Player getAiPlayer(){
        return AiPlayer;
    }

    public static void updatePlayer1Health(GameState gameState,ActorRef out,int healthnumber){
        //update the health of unit and avatar 
        int health=humanPlayer.getHealth();
        int new_health=health+healthnumber;
        if(new_health>0 && new_health<=20){
            humanPlayer.setHealth(new_health);
            BasicCommands.setPlayer1Health(out, humanPlayer);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            gameState.humanPlayer.setHealth(new_health);;
            BasicCommands.setUnitHealth(out, gameState.humanPlayer, new_health);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        } else if(new_health<=0){
            humanPlayer.setHealth(0);
            BasicCommands.setPlayer1Health(out,humanPlayer);
            gameState.humanPlayer.setHealth(0);
            BasicCommands.setUnitHealth(out, gameState.humanPlayer,0);
            BasicCommands.playUnitAnimation(out, gameState.humanPlayer, UnitAnimationType.death);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            BasicCommands.deleteUnit(out, gameState.humanPlayer);
            checkWinner(gameState,out);
        }
    }

    public static void updatePlayer2Health(GameState gameState,ActorRef out,int healthnumber){
        //update the health of unit and avatar 
        int health=AiPlayer.getHealth();
        int new_health=health+healthnumber;
        if(new_health>0 && new_health<=20){
            AiPlayer.setHealth(new_health);
            BasicCommands.setPlayer2Health(out, AiPlayer);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            gameState.aiPlayer.setHealth(new_health);;
            BasicCommands.setUnitHealth(out, gameState.aiPlayer, new_health);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        } else if(new_health<=0){
            AiPlayer.setHealth(0);
            BasicCommands.setPlayer2Health(out,AiPlayer);
            gameState.aiPlayer.setHealth(0);
            BasicCommands.setUnitHealth(out, gameState.aiPlayer, 0);
            BasicCommands.playUnitAnimation(out, gameState.aiPlayer, UnitAnimationType.death);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            BasicCommands.deleteUnit(out, gameState.aiPlayer);
            checkWinner(gameState,out);
        }
        if(healthnumber<0){
            UnitAbilities.Zeal(gameState, out, gameState.aiPlayer);
        }
    }

    public static void checkWinner(GameState gameState, ActorRef out){
        if(humanPlayer.getHealth()<=0){
            BasicCommands.addPlayer1Notification(out, "Game Over - AI wins!", 2);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            gameState.something=false;
        }else if(AiPlayer.getHealth()<=0){
            BasicCommands.addPlayer1Notification(out, "Game Over - Player 1 wins!", 2);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            gameState.something=false;
        }
    }
}
