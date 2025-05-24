package structures;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.BasicCommands;
import events.TileClicked;
import structures.basic.*;
import utils.BasicObjectBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UnitManagement {

    public static void UnitIntialize(ActorRef out,Card card,Unit unit){
        BigCard bigCard = card.getBigCard();
        int health=bigCard.getHealth();
        int attack=bigCard.getAttack();
        unit.setAttack(attack);
        unit.setHealth(health);
        unit.setName(card.getCardname());
        BasicCommands.setUnitAttack(out, unit, attack);
        BasicCommands.setUnitHealth(out, unit, health);
    }

    public static boolean canMove(Unit unit){
        if(unit.isStunned()){
            return false;
        }else if(unit.hasRush()){
            return true;
        }else if(unit.isNewSummon()){
            return false;
        }else if(unit.hasAttacked()){
            return false;
        }else if(unit.isProvoked()){
            return false;
        }else if(!unit.hasMoved()){
            return true;
        }else{
            return false;
        }
    }
    public static boolean canAttack(Unit unit){
        if(unit.isStunned()){
            return false;
        }else if(unit.isNewSummon()){
            return false;
        }else if(unit.hasRush()){
            return true;
        }else if(!unit.hasAttacked()){
            return true;
        }else{
            return false;
        }
    }
    public static void move(ActorRef out,GameState gameState,Tile targetTile,Unit unit){
        // playUnitAnimation [Move]
        BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.move);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        
        //move to the target tile
        Tile start=BasicObjectBuilders.loadTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
        int dx=Integer.compare(targetTile.getTilex(), start.getTilex());
        int dy=Integer.compare(targetTile.getTiley(), start.getTiley());
        Tile unit1=BasicObjectBuilders.loadTile(start.getTilex()+dx, start.getTiley());
        Tile unit2=BasicObjectBuilders.loadTile(start.getTilex(), start.getTiley()+dy);
        if(Board.getUnitFromTile(gameState, out, unit1)!=null){
            BasicCommands.addPlayer1Notification(out, "moveUnitToTile", 2);
            BasicCommands.moveUnitToTile(out, unit, targetTile, true);
            try {Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
        }else{
            BasicCommands.addPlayer1Notification(out, "moveUnitToTile", 2);
            BasicCommands.moveUnitToTile(out, unit, targetTile);
            try {Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
        }

        //after the moving,change the position to the target tile
        unit.setPositionByTile(targetTile);
        unit.setMove(true);
    }
    public static void attack(ActorRef out,GameState gameState,Unit attacker,Unit target){
        if(canAttack(attacker)){
            Tile targetTile=BasicObjectBuilders.loadTile(target.getPosition().getTilex(), target.getPosition().getTiley());
            // playUnitAnimation [Attack]
            BasicCommands.addPlayer1Notification(out, "attack", 2);
            BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            TileClicked.damageEffect(gameState,out,target,attacker.getAttack());
            attacker.setAttacked(true);

            //defensive,check the target if dead
            if(Board.getUnitFromTile(gameState, out, targetTile)!=null){
                BasicCommands.addPlayer1Notification(out, "defensive", 2);
                BasicCommands.playUnitAnimation(out, target, UnitAnimationType.attack);
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                TileClicked.damageEffect(gameState,out,attacker,target.getAttack());
            }
        }
    }
    public static void startNewTurn(ActorRef out,GameState gameState) {
        List<Unit> allUnitList=new ArrayList<>();
        allUnitList.addAll(gameState.aiPlayerUnitList);
        allUnitList.addAll(gameState.humanPlayerUnitList);
        for(Unit unit:allUnitList){
            int stunDuration=unit.getStunDuration();
            if(stunDuration>0){
                unit.setStunDuration(stunDuration-1);
                if(unit.getStunDuration()==0){
                    unit.setStun(false);
                }
            }
            unit.setMove(false);
            unit.setAttacked(false);
            unit.setNewSummon(false);
            unit.setProvoked(false);
        }
    }
    public static List<Unit> getAllUnitOfProvoke(GameState gameState){
        List<Unit> provokeList=new ArrayList<>();
        List<Unit> allUnits=new ArrayList<>();
		allUnits.addAll(gameState.aiPlayerUnitList);
		allUnits.addAll(gameState.humanPlayerUnitList);
		for(Unit unit:allUnits){
			if(unit.getName()!=null){
                if(unit.getName().equals("Rock Pulveriser")||unit.getName().equals("Swamp Entangler")||unit.getName().equals("Ironcliff Guardian")){
                    provokeList.add(unit);
                }
			}
		}
        return provokeList;
    }
    public static void setProvoke(ActorRef out,GameState gameState){
        // get all the unit with provoke ability
        List<Unit> provokeList=getAllUnitOfProvoke(gameState);
        int provokeCount=provokeList.size();
        if(provokeCount>0){
            for(Unit unit:provokeList){
                Tile tile=BasicObjectBuilders.loadTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
                for(Unit enemy:UnitAbilities.provokeEnemyList(gameState, out, tile)){
                    enemy.setProvoked(true);
                }
            }
        }
    }
}
