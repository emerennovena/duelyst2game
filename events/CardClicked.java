package events;


import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import structures.basic.Tile;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.*;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a card.
 * The event returns the position in the player's hand the card resides within.
 * 
 * { 
 *   messageType = “cardClicked”
 *   position = <hand index position [1-6]>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class CardClicked implements EventProcessor{
	Unit isPlayer1Unit;
	private static int handPosition;
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		if(gameState.something==true){

			handPosition = message.get("position").asInt();
			gameState.handPosition=handPosition;

			Unit humanPlayerUnit=gameState.humanPlayer;
			Unit aiPlayerUnit = gameState.aiPlayer;
			Card card=CardManagement.getCardInformation(handPosition);
			Player humanPlayer=PlayerManagement.getHumanPlayer();


			Board.clearHighlighting(out);
			gameState.readyToMove=null;
			gameState.readyToAttack=null;
			// creature card
			if(checkMana(humanPlayer,card)==true && card.isCreature()==true){
				CardManagement.activeCard(out,handPosition);
				Board.summonHighlighting(gameState,out);
			// spell card
			}else if(checkMana(humanPlayer,card)==true && card.isCreature()==false){
				CardManagement.activeCard(out,handPosition);
				if(card.getCardname().equals("Horn of the Forsaken")) {
					Board.humanAvatarHighlighting(gameState,out);
					gameState.isSpellTarget=1;
				}
				else if(card.getCardname().equals("Wraithling Swarm")) {
					Board.humanAvatarHighlighting(gameState,out);
					gameState.isSpellTarget=1;
				}
				else if(card.getCardname().equals("Dark Terminus")) {
					Board.attackHighlighting(out,gameState,aiPlayerUnit);
					gameState.isSpellTarget=1;
				}
			}
		}
	}

	public static boolean checkMana(Player player,Card card){
		if(player.getMana()>=card.getManacost()){
			return true;
		}return false;
	}

}
