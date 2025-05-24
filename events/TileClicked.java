package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.*;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import java.util.*;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a tile.
 * The event returns the x (horizontal) and y (vertical) indices of the tile that was
 * clicked. Tile indices start at 1.
 * 
 * { 
 *   messageType = “tileClicked”
 *   tilex = <x index of the tile>
 *   tiley = <y index of the tile>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class TileClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();
		Tile tile=BasicObjectBuilders.loadTile(tilex, tiley);
		if (gameState.something == true) {
			if(isPlayer1Unit(gameState, tilex, tiley)!=null){
				Unit unit=isPlayer1Unit(gameState, tilex, tiley);

				if(gameState.isSpellTarget==0){
					if(UnitManagement.canMove(unit)){
						Board.clearHighlighting(out);
						if(unit.hasMoved()==false && gameState.readyToMove==null && gameState.readyToAttack==null){
							// if is not move, then choose the target tile to move
							System.out.println("move hightling");
							gameState.startTile=tile;
							Board.moveHighlighting(out, gameState, unit);
						}else if(unit.hasMoved()==true && unit.hasAttacked()==false && gameState.readyToAttack==null){
							System.out.println("attack hightling");
							// if already move,then start to attack,highting the target attack
							Board.attackHighlighting(out, gameState, unit);
						}
					}else{
						BasicCommands.addPlayer1Notification(out, "this unit can not move", 2);
						gameState.readyToMove=null;
						Board.clearHighlighting(out);
						if(unit.hasAttacked()==false && gameState.readyToAttack==null){
							System.out.println("attack hightling");
							// if can not move,then start to attack,highting the target attack
							Board.attackHighlighting(out, gameState, unit);
						}
					}
				}else{
					System.out.println("use spell");
					drawUnit(gameState,out,tile);
					Board.clearHighlighting(out);
					gameState.isSpellTarget=0;
				}
			}else if(isPlayer2Unit(gameState, tilex, tiley)!=null){
				// if click the enemy unit, if already move,then attack
				if(gameState.readyToAttack!=null){
					Board.clearHighlighting(out);
					Unit unit=gameState.readyToAttack;
					Unit targetUnit=Board.getUnitFromTile(gameState, out, tile);
					UnitManagement.attack(out, gameState, unit, targetUnit);
					gameState.readyToAttack=null;
				}else if(isPlayer2Unit(gameState, tilex, tiley).hasMoved()==false && gameState.readyToAttack==null){
					//is not move and click the enemy,need to move and attack
					UnitMoveAndAttack(gameState,out,tile);
				}
			}else{
				//if click the empty tile,if the startTile is not null then move 
				if(gameState.startTile!=null){
					UnitManagement.move(out, gameState, tile, gameState.readyToMove);
					Board.clearHighlighting(out);
					gameState.startTile=null;
					gameState.readyToMove=null;
				}else{
					if(gameState.handPosition!=0){
						drawUnit(gameState,out,tile);
						Board.clearHighlighting(out);
					}
				}
			}

		}
		
	}

	public static void drawUnit(GameState gameState,ActorRef out,Tile tile) {
		//get the card information 
		int handPosition=gameState.handPosition;
		Card card=CardManagement.getCardInformation(handPosition);
		if(card.isCreature()==true){
			//create unit and set the health and attack of the unit
			Unit unit = BasicObjectBuilders.loadUnit(card.getUnitConfig(), gameState.unitId, Unit.class);
			gameState.unitId++;
			unit.setPositionByTile(tile);
			BasicCommands.drawUnit(out, unit, tile);
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
			UnitManagement.UnitIntialize(out, card, unit);
			UnitAbilities.openingGambit(gameState, out, tile);

			// setting a unit to have the flying ability
			if(card.hasFlying()) {
				unit.setFlying(true);
			}
			// logic to give a unit with rush the ability to attack without movement
			if (card.hasRush()){
				unit.setRush(true);
			}
			gameState.humanPlayerUnitList.add(unit);
			reduceManaAndRemoveCard(gameState, out, card, handPosition);
		}
		
		//Humanplayer's spells
	
		if(card.getCardname().equals("Horn of the Forsaken")){
			Spell.hornOfTheForsaken(gameState, out, tile);
			reduceManaAndRemoveCard(gameState, out, card, handPosition);
		}
		
		if(card.getCardname().equals("Dark Terminus")){
			Spell.darkTerminus(gameState, out, tile);
			reduceManaAndRemoveCard(gameState, out, card, handPosition);
		}
		
		if(card.getCardname().equals("Wraithling Swarm")){
			Spell.wraithlingSwarm(gameState, out, tile);
			reduceManaAndRemoveCard(gameState, out, card, handPosition);
		}
	
	}
	
	public static Unit isPlayer1Unit(GameState gameState, int tilex,int tiley){
		List<Unit> player1_unitList = gameState.humanPlayerUnitList;
		for(Unit unit:player1_unitList){
			if(unit.getPosition().getTilex()==tilex && unit.getPosition().getTiley()==tiley){
				return unit;
			}
		}
		return null;
	}
	public static Unit isPlayer2Unit(GameState gameState, int tilex,int tiley){
		List<Unit> player2_unitList = gameState.aiPlayerUnitList;
		for(Unit unit:player2_unitList){
			if(unit.getPosition().getTilex()==tilex && unit.getPosition().getTiley()==tiley){
				return unit;
			}
		}
		return null;
	}
	
	public static void UnitMoveAndAttack(GameState gameState, ActorRef out,Tile tile){
		//calculate the tile 
		List<Tile> possibleAttackTile=new ArrayList<>();
		Tile startTile=gameState.startTile;


		for(Tile t:gameState.moveTileList){
			int stepsX = Math.abs(tile.getTilex() - startTile.getTilex());
			int stepsY = Math.abs(tile.getTiley() - startTile.getTiley());
			int totalSteps = Math.max(stepsX, stepsY);

			Unit player1_unit=isPlayer1Unit(gameState, t.getTilex(), t.getTiley());
			Unit player2_unit=isPlayer2Unit(gameState, t.getTilex(), t.getTiley());
			if(player1_unit==null && player2_unit==null && totalSteps>1){
				int Tilex=tile.getTilex();
				int Tiley=tile.getTiley();
				if(t.getTilex()>=Tilex-1 && t.getTilex()<=Tilex+1 && t.getTiley()>=Tiley-1 && t.getTiley()<=Tiley+1){
					if(t.getTiley()==Tiley || t.getTilex()==Tilex){
						possibleAttackTile.add(t);
					}
				}
			}
		}
		if(possibleAttackTile.size()>0){
			//move first
			Tile t=possibleAttackTile.get(0);
			UnitManagement.move(out, gameState, t, gameState.readyToMove);
			// gameState.isMoved=1;
		}else{
			gameState.readyToMove.setMove(true);
			// gameState.isMoved=1;
		}
		Board.clearHighlighting(out);
		gameState.moveTileList=new ArrayList<>();
		gameState.startTile=null;
		
		// attack
		Unit targetUnit=Board.getUnitFromTile(gameState, out, tile);
		UnitManagement.attack(out, gameState, gameState.readyToMove, targetUnit);
		// gameState.isAttacked=1;
		gameState.readyToMove=null;
	}

	public static void damageEffect(GameState gameState, ActorRef out,Unit unit,int damageNumber){
		int x=unit.getPosition().getTilex();
		int y=unit.getPosition().getTiley();
		if(unit.getName()==null && isPlayer1Unit(gameState,x,y)!=null){
			PlayerManagement.updatePlayer1Health(gameState, out, -damageNumber);
		}else if(unit.getName()==null && isPlayer2Unit(gameState,x,y)!=null){
			PlayerManagement.updatePlayer2Health(gameState, out, -damageNumber);
		}else{
			UnitAbilities.damage(gameState, out,unit,damageNumber);
		}
	}

	private static void reduceManaAndRemoveCard(GameState gameState, ActorRef out, Card card, int handPosition) {
	    Player humanPlayer = PlayerManagement.getHumanPlayer();
	    int mana = humanPlayer.getMana() - card.getManacost();
	    PlayerManagement.setPlayer1Mana(out, mana);

	    CardManagement.removeCard(out, handPosition);
	    gameState.handPosition = 0;
	}
}
