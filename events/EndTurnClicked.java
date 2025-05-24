package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;
import structures.basic.*;
import structures.*;


public class EndTurnClicked implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {


        if(gameState.something==true){
            if(TurnManagement.getCurrentPlayer().getPlayerName().equals("HumanPlayer")){
                TurnManagement.endTurn(out, gameState);
                if(TurnManagement.getCurrentPlayer().getPlayerName().equals("AiPlayer")){
                    AICompute.aiPlay(out,gameState);
                }
            }
        }else{
            System.out.println("start new game");
            Initalize.start(out, gameState);
        }
    }


}