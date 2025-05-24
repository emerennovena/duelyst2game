package events;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import main.Main;
import structures.*;
import structures.basic.*;
import utils.*;

/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * { 
 *   messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		
		gameState.gameInitalised = true;
		
		gameState.something = true;

		//draw board
        Board.boardInitialize(gameState,out);

       	//draw two players
       	PlayerManagement player=new PlayerManagement(gameState,out);

		
		//start Turn
		TurnManagement turn =new TurnManagement(PlayerManagement.getHumanPlayer(), PlayerManagement.getAiPlayer());
		turn.startTurn(out, gameState);

		// Main.executeDemo(out, gameState, message);

	}

	public static void start(ActorRef out, GameState gameState){
		gameState.gameInitalised = true;
		
		gameState.something = true;

       	//draw two players
       	PlayerManagement player=new PlayerManagement(gameState,out);

		//reset all gameState
		resetGameState(gameState);


		//start Turn
		TurnManagement turn =new TurnManagement(PlayerManagement.getHumanPlayer(), PlayerManagement.getAiPlayer());
		turn.startTurn(out, gameState);
	}

	public static void resetGameState(GameState gameState){
		gameState.startTile=null;
		gameState.humanPlayerUnitList=new ArrayList<>();
		gameState.aiPlayerUnitList=new ArrayList<>();
		gameState.humanPlayer=null;
		gameState.aiPlayer=null;
		gameState.isMoved=0;
		gameState.readyToMove=null;
		gameState.isAttacked=0;
		gameState.readyToAttack=null;
		gameState.moveTileList=new ArrayList<>();
		gameState.board=new ArrayList<>();
		gameState.handPosition=0;
		gameState.unitId=0;
		gameState.provokeUnit=null;
		gameState.isSpellTarget=0;
		gameState.isHumanAttacked=0; 
		gameState.keepOriginalHealth=0; 
		gameState.aiPlayerCardList=OrderedCardLoader.getPlayer2Cards(2); 
	}

}


