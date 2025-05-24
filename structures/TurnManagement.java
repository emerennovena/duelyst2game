package structures;

import commands.BasicCommands;
import structures.basic.*;
import akka.actor.ActorRef;

public class TurnManagement {
    private static int turnNumber=0;
    private static Player huamPlayer;
    private static Player aiPlayer;
    private static Player currentTurnPlayer;

    public TurnManagement(Player humanPlayer,Player aiPlayer){
        this.huamPlayer=humanPlayer;
        this.aiPlayer=aiPlayer;
        this.currentTurnPlayer=humanPlayer;
    }

    public static void addTurn(){
        turnNumber++;
    }

    public static int getTurnNumber(){
        return turnNumber;
    }

    public static Player getCurrentPlayer(){
        return currentTurnPlayer;
    }

    public static void endTurn(ActorRef out,GameState gameState){
        //change to next player
        currentTurnPlayer = (currentTurnPlayer.getPlayerName().equals("HumanPlayer")) ? aiPlayer : huamPlayer;

        //reset
        UnitManagement.startNewTurn(out, gameState);
        UnitManagement.setProvoke(out, gameState);
        resetGameState(gameState);

        //create notification
        String notification=String.format("Turn %d : %s 's turn ", turnNumber,currentTurnPlayer.getPlayerName());
        BasicCommands.addPlayer1Notification(out, notification, 5);
    }

    public static void startTurn(ActorRef out,GameState gameState){
        if(turnNumber>0){
            //change to next player
            currentTurnPlayer = (currentTurnPlayer.getPlayerName().equals("HumanPlayer")) ? aiPlayer : huamPlayer;
        }

        // start new turn
        addTurn();
        PlayerManagement.setPlayer1Mana(out,turnNumber+1);
        PlayerManagement.setPlayer2Mana(out,turnNumber+1);
        if(turnNumber==1){
            //draw card
       	    CardManagement.cardInitialize(out);
        }else{
            CardManagement.removeCardFromDeck(out);
        }

        //create notification
        String notification=String.format("Turn %d : %s 's turn ", turnNumber,currentTurnPlayer.getPlayerName());
        BasicCommands.addPlayer1Notification(out, notification, 5);

        UnitManagement.startNewTurn(out, gameState);
        UnitManagement.setProvoke(out, gameState);
        resetGameState(gameState);
    }
    public static void resetGameState(GameState gameState){
        gameState.isAttacked=0;
        gameState.isMoved=0;
        gameState.readyToAttack=null;
        gameState.readyToMove=null;
    }
}
