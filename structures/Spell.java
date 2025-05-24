package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.CardClicked;
import events.TileClicked;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Spell {

		//Horn of the Forsaken :Artifact 3, On Hit
		//Horn of the Forsaken To do : add a counter: 세번을 어떻게 적용?
	   //dark terminus to do : : (check if it is an AI unit) & AI unit : non-avatar highlighting
	
	public static void hornOfTheForsaken(GameState gameState, ActorRef out, Tile tile) {
		
	    Unit unit = Board.getUnitFromTile(gameState, out, tile);
		//Artifact 3
	    //player’s avatar with 3 robustness.
	    //even if the avatar is attacked, the health should not be deducted -> keep the health before it is attacked 

	    if (gameState.isHumanAttacked == 1) {
            int new_health=gameState.keepOriginalHealth;
            unit.setHealth(new_health);
            BasicCommands.setUnitHealth(out, unit, new_health);
			BasicCommands.addPlayer1Notification(out, "Artifact 3", 2);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

	    }
		//On Hit
		//whenever the avatar attacks unit, summon 1 Wraithling on a random tile.
	    if (gameState.isAttacked == 1) {
	        UnitAbilities.summonWraithlingByRandom(gameState, out, tile);
	    }

	}
	
	
//    Wraithling Swarm : select the human avatar -> Summon 3 Wraithlings in sequence on random tiles
	public static void wraithlingSwarm(GameState gameState,ActorRef out,Tile tile) {
	    Unit unit = Board.getUnitFromTile(gameState, out, tile);
	    int tilex = unit.getPosition().getTilex();
	    int tiley = unit.getPosition().getTiley();

	    List<Tile> targetTiles = new ArrayList<>();
	    // Identify all empty adjacent tiles
	    for (int i = tilex - 1; i <= tilex + 1; i++) {
	        for (int j = tiley - 1; j <= tiley + 1; j++) {
	            Tile t = BasicObjectBuilders.loadTile(i, j);
	            if (Board.getUnitFromTile(gameState, out, t) == null) {
	                targetTiles.add(t);
	            }
	        }
	    }

	    // If there are enough empty tiles, summon 3 units
	    if (targetTiles.size() >= 3) {
	        Random random = new Random();

	        // Summon 3 units
	        for (int i = 0; i < 3; i++) {
	            int n = random.nextInt(targetTiles.size());
	            Tile summonTile = targetTiles.get(n);
	            targetTiles.remove(n); // Remove the chosen tile to prevent reusing it

	            // Create and configure the Wraithling unit
	            Unit wraithling = BasicObjectBuilders.loadUnit(StaticConfFiles.wraithling, gameState.unitId, Unit.class);
	            gameState.unitId++;
	            wraithling.setPositionByTile(summonTile);
	            wraithling.setAttack(1);
	            wraithling.setHealth(1);

	            // Draw the unit and set its properties
	            BasicCommands.drawUnit(out, wraithling, summonTile);
	            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
	            BasicCommands.setUnitAttack(out, wraithling, 1);
	            BasicCommands.setUnitHealth(out, wraithling, 1);

	            // Add the summoned unit to the human player's unit list
	            gameState.humanPlayerUnitList.add(wraithling);
	            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
	        }
	    }
	}

    //Dark Terminus : Destroy an enemy creature. Summon a Wraithling on the tile of the destroyed creature.
   //to do : (check if it is an AI unit) & AI unit : non-avatar highlighting
    public static void darkTerminus(GameState gameState,ActorRef out,Tile tile) {
        
    	// select an AI to remove -> get the position
        Unit targetEnemy =Board.getUnitFromTile(gameState, out, tile);
        int tilex=targetEnemy.getPosition().getTilex();
        int tiley=targetEnemy.getPosition().getTiley();
        
        Tile summonTile=BasicObjectBuilders.loadTile(tilex, tiley);
        
        Unit player2_unit=TileClicked.isPlayer2Unit(gameState, tile.getTilex(), tile.getTiley());
        //check if it is an AI unit : && targetEnemy == player2_unit
        if (targetEnemy != null ) {
            // remove the creature
        	targetEnemy.setHealth(0);
            BasicCommands.playUnitAnimation(out, targetEnemy, UnitAnimationType.death);
    		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            BasicCommands.deleteUnit(out, targetEnemy);
            gameState.aiPlayerUnitList.remove(targetEnemy);
			UnitAbilities.deathwatch(gameState, out);
        	
            //then, place Wraithling on the tile
            Unit wraithling = BasicObjectBuilders.loadUnit(StaticConfFiles.wraithling, gameState.unitId, Unit.class);
            gameState.unitId++;
            wraithling.setPositionByTile(summonTile);
            wraithling.setAttack(1);
            wraithling.setHealth(1);
            BasicCommands.drawUnit(out, wraithling, summonTile);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            
            BasicCommands.setUnitAttack(out, wraithling, 1);
            BasicCommands.setUnitHealth(out, wraithling, 1);
            gameState.humanPlayerUnitList.add(wraithling);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        }
    }
    
            

//AI's spells

//Sundrop Elixir
//Select an AI unit -> health +4 / not increase its maximum health
	public static void sundropElixir(GameState gameState,ActorRef out,Tile tile) {
	    Unit player2_unit=TileClicked.isPlayer2Unit(gameState, tile.getTilex(), tile.getTiley());
	    Unit unitToHeal = Board.getUnitFromTile(gameState, out, tile);

		if (unitToHeal!= null && player2_unit!=unitToHeal) {
		            int health=unitToHeal.getHealth()+4;
		            if (health<=16) {//maximum health = 20?
		            	unitToHeal.setHealth(health);
		            	BasicCommands.setUnitHealth(out, unitToHeal, health);}
		            else if (health>=17) {
		            	unitToHeal.setHealth(20);
		            }
		            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		        }
		    }

//True Strike
//Select a human unit -> health -2
	public static void trueStrike(GameState gameState,ActorRef out,Tile tile) {	
			Unit player1_unit=TileClicked.isPlayer1Unit(gameState, tile.getTilex(), tile.getTiley());
			Unit unitToDeal = Board.getUnitFromTile(gameState, out, tile);
			   
			if (unitToDeal != null && player1_unit != unitToDeal) {
			            int health=unitToDeal.getHealth()-2;
			            if (health>=2) {
			            	unitToDeal.setHealth(health);
			            	BasicCommands.setUnitHealth(out, unitToDeal, health);}
			         // if health <0 -> die: remove the unit
			            else {
			            	unitToDeal.setHealth(0);
			                BasicCommands.playUnitAnimation(out, unitToDeal, UnitAnimationType.death);
			        		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
			                BasicCommands.deleteUnit(out, unitToDeal);
			                gameState.humanPlayerUnitList.remove(unitToDeal);
							UnitAbilities.deathwatch(gameState, out);
			            	
			            }
			            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
			        }
			    }


//Beam Shock
//Cost = 0
//Effects: Stun (the target unit cannot move or attack next turn) target enemy non- avatar unit

	public static void beamShock(GameState gameState,ActorRef out,Tile tile) {
//		Unit player2_unit=TileClicked.isPlayer2Unit(gameState, tile.getTilex(), tile.getTiley());
		Unit unitToStop = Board.getUnitFromTile(gameState, out, tile);
		gameState.readyToMove = unitToStop;
		
		if (unitToStop != null) {
	        gameState.readyToMove=null;
	        gameState.readyToAttack=null;
	        gameState.startTile=null;
	        gameState.isMoved=0;
	        gameState.isAttacked=0;
			}
		}
	}
	    
	   