package main;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.TileClicked;
import structures.*;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

public class Main {

	public static String cardsDIR = "conf/gameconfs/cards/";
	public static int cardId=0;

    public static void executeDemo(ActorRef out, GameState gameState,JsonNode message) {



		//bugfix1: humanplayer [3,2],aiplayer[5,2],in the middle here has a unit, humanplayer move highlighting

		//humanplayer unit:
		// Unit unit_humanPlayer = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, gameState.unitId, Unit.class);
        // gameState.unitId++;
        // Tile tile_humanPlayer=BasicObjectBuilders.loadTile(4, 2);
        // unit_humanPlayer.setPositionByTile(tile_humanPlayer);
        // unit_humanPlayer.setHealth(20);
        // BasicCommands.drawUnit(out, unit_humanPlayer, tile_humanPlayer);
        // try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        // //set the health of the unit
        // BasicCommands.setUnitHealth(out,unit_humanPlayer,20);
		// gameState.humanPlayerUnitList.add(unit_humanPlayer);

		//ai player unit:
		// Unit unit_aiPlayer = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, gameState.unitId, Unit.class);
        // gameState.unitId++;
        // Tile tile_aiPlayer=BasicObjectBuilders.loadTile(5, 2);
        // unit_aiPlayer.setPositionByTile(tile_aiPlayer);
        // unit_aiPlayer.setHealth(20);
        // BasicCommands.drawUnit(out, unit_aiPlayer, tile_aiPlayer);
        // try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        // //set the health of the unit
        // BasicCommands.setUnitHealth(out,unit_aiPlayer,20);
		// gameState.aiPlayerUnitList.add(unit_aiPlayer);


		//draw unit
        // Tile tile1 = BasicObjectBuilders.loadTile(3, 4);
		// Card card1=BasicObjectBuilders.loadCard(cardsDIR+"1_9_c_u_bloodmoon_priestess.json", cardId, Card.class);
		// cardId++;
        // Unit unit1 = BasicObjectBuilders.loadUnit(card1.getUnitConfig(), gameState.unitId, Unit.class);
		// gameState.unitId++;
        // unit1.setPositionByTile(tile1);
        // BasicCommands.drawUnit(out, unit1, tile1);
		// try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		// UnitManagement.UnitIntialize(out, card1, unit1);
		// gameState.humanPlayerUnitList.add(unit1);
        // try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}


		// AICompute.aiPlay(out, gameState);

		// UnitAbilities.deathwatch(gameState, out);

		// PlayerManagement.updatePlayer1Health(gameState, out, -2);
		// UnitManagement.startNewTurn(out, gameState);

		// Board.moveHighlighting(out, gameState, unit_humanPlayer);

		// PlayerManagement.updatePlayer2Health(gameState, out, -2);

		// Tile tile2 = BasicObjectBuilders.loadTile(6, 1);
		// Card card2=BasicObjectBuilders.loadCard(cardsDIR+"1_a1_c_u_shadowdancer.json", cardId, Card.class);
		// cardId++;
        // Unit unit2 = BasicObjectBuilders.loadUnit(card2.getUnitConfig(), gameState.unitId, Unit.class);
		// gameState.unitId++;
        // unit2.setPositionByTile(tile2);
        // BasicCommands.drawUnit(out, unit2, tile2);
		// try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		// UnitManagement.UnitIntialize(out, card2, unit2);
		// gameState.humanPlayerUnitList.add(unit2);
        // try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
	
		// UnitAbilities.deathwatch(gameState, out);

		// Tile tile3 = BasicObjectBuilders.loadTile(5, 2);
		// Card card3=BasicObjectBuilders.loadCard(cardsDIR+"2_2_c_u_swamp_entangler.json", cardId, Card.class);
		// cardId++;
        // Unit unit3 = BasicObjectBuilders.loadUnit(card3.getUnitConfig(), gameState.unitId, Unit.class);
		// gameState.unitId++;
        // unit3.setPositionByTile(tile3);
        // BasicCommands.drawUnit(out, unit3, tile3);
		// try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		// UnitManagement.UnitIntialize(out, card3, unit3);
		// gameState.aiPlayerUnitList.add(unit3);
        // try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		// UnitAbilities.openingGambit(gameState, out, tile3);

		// AICompute.abilities(out, gameState);

		//deathwatch
		// System.out.println("deathwatch");
		// UnitAbilities.deathwatch(gameState, out);


		// AICompute.abilities(out, gameState);


	
		// Tile tile4 = BasicObjectBuilders.loadTile(8, 2);
		// Card card4=BasicObjectBuilders.loadCard(cardsDIR+"1_3_c_u_gloom_chaser.json", cardId, Card.class);
		// cardId++;
        // Unit unit4 = BasicObjectBuilders.loadUnit(card4.getUnitConfig(), gameState.unitId, Unit.class);
		// gameState.unitId++;
        // unit4.setPositionByTile(tile4);
        // BasicCommands.drawUnit(out, unit4, tile4);
		// try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		// UnitManagement.UnitIntialize(out, card4, unit4);
		// gameState.aiPlayerUnitList.add(unit4);
        // try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}


		// UnitManagement.startNewTurn(out, gameState);
		// UnitManagement.moveHighlighting(out, gameState, unit1);
		// UnitManagement.move(out, gameState, BasicObjectBuilders.loadTile(7, 1), unit1);
		// UnitManagement.setProvoke(out, gameState);
		// Board.moveHighlighting(out, gameState, unit1);
		// Board.attackHighlighting(out, gameState, unit1);
		// System.out.println("attacktarget:");
		// UnitManagement.getAttackTarget(out, gameState, unit1);


		//ability
		// UnitAbilities.gainHealth(gameState, out, tile1, 4);
		// UnitAbilities.gainAttack(gameState, out, tile1, 3);
		// UnitAbilities.summonWraithlingBehind(gameState, out);
		// UnitAbilities.destroyAdjacentUnit(gameState, out, tile1);
		// UnitAbilities.summonWraithlingByRandom(gameState,out,tile1);
		// UnitAbilities.damage(gameState, out, tile4, 10);
		// player.updatePlayer1Health(gameState,out, -2);
		// player.updatePlayer1Health(gameState,out, 1);
		// UnitAbilities.gainEffectsAdjacentUnit(gameState,out,tile1);
		// UnitAbilities.flying(gameState, out);
		// UnitAbilities.provoke(gameState, out, tile1);
		

		// List<Unit> provokeEnemyList =UnitAbilities.provokeEnemyList(gameState, out, tile2);
		// System.out.println(provokeEnemyList.size());
		// for(Unit unit:provokeEnemyList){
		// 	System.out.println(String.format("unit name: %s , x: %d, y: %d", unit.getName(),unit.getPosition().getTilex(),unit.getPosition().getTiley()));
		// }

		// List<Unit> provokeAttackList =UnitAbilities.provokeAttackList(gameState, out, tile1);
		// for(Unit unit:provokeAttackList){
		// 	System.out.println(String.format("unit name: %s , x: %d, y: %d", unit.getName(),unit.getPosition().getTilex(),unit.getPosition().getTiley()));
		// }

		//ai part
		// AICompute.aiPlay(out, gameState);


		//spell ability testing
		// Tile targetTile = BasicObjectBuilders.loadTile(6, 2);
		// Card spellCard1=BasicObjectBuilders.loadCard(cardsDIR+"2_9_c_s_sundrop_elixir.json", 20+cardId, Card.class);
		// cardId++;
		// AICompute.playSpell(out, gameState, targetTile, spellCard1);

		// try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		// Card spellCard2=BasicObjectBuilders.loadCard(cardsDIR+"2_a1_c_s_truestrike.json", 20+cardId, Card.class);
		// cardId++;
		// AICompute.playSpell(out, gameState, targetTile, spellCard2);

		// Tile targetTile = BasicObjectBuilders.loadTile(6, 1);
		// Card spellCard2=BasicObjectBuilders.loadCard(cardsDIR+"2_5_c_s_beamshock.json", 20+cardId, Card.class);
		// cardId++;
		// AICompute.playSpell(out, gameState, targetTile, spellCard2);
		// Unit target=Board.getUnitFromTile(gameState, out, targetTile);
		// System.out.println("is stun: "+target.isStunned());
		// System.out.println("can move: "+UnitManagement.canMove(target));
		// System.out.println("can attack: "+UnitManagement.canAttack(target));
        
    }

}
