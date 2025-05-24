package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.TileClicked;
import structures.basic.*;
import utils.BasicObjectBuilders;

import java.util.*;



public class Board { 
    

    public static void boardInitialize(GameState gameState,ActorRef out){
        BasicCommands.addPlayer1Notification(out, "drawTile", 2);
        for(int i=0;i<9;i++){
            for(int j=0;j<5;j++){
                Tile tile = BasicObjectBuilders.loadTile(i, j);
                BasicCommands.drawTile(out, tile, 0);
                gameState.board.add(tile);
            }
        }
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void summonHighlighting(GameState gameState,ActorRef out){
        LinkedHashSet<Tile> summonTileList= new LinkedHashSet<>();
        //find all human player's unit
        for(Unit unit:gameState.humanPlayerUnitList){
            summonTileList.addAll(getSummonTile(out,gameState,unit));
        }
        for(Tile t:summonTileList){
            BasicCommands.drawTile(out, t, 1);
        }
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void moveHighlighting(ActorRef out,GameState gameState,Unit unit){
        if(UnitManagement.canMove(unit)){
            List<Tile> moveTileList=getMoveTile(out, gameState, unit);
            if( unit.getName()!=null && unit.getName().equals("Young Flamewing")){
                for(Tile end:moveTileList){
                    BasicCommands.drawTile(out, end, 1);
                }
            }else{
                Tile start=BasicObjectBuilders.loadTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
                for(Tile end:moveTileList){
                    if(TileClicked.isPlayer2Unit(gameState, end.getTilex(), end.getTiley())!=null){
                        int stepsX = Math.abs(end.getTilex() - start.getTilex());
                        int stepsY = Math.abs(end.getTiley() - start.getTiley());
                        int totalSteps = Math.max(stepsX, stepsY);
                        if(totalSteps==1){
                            BasicCommands.drawTile(out, end, 2);
                        }else if(totalSteps>1 && !Board.isStuck(start, end, out, gameState)){
                            BasicCommands.drawTile(out, end, 2);
                        }
                    }else if(!Board.isStuck(start, end, out, gameState) && Board.getUnitFromTile(gameState, out, end)==null){
                        BasicCommands.drawTile(out, end, 1);
                        gameState.moveTileList.add(end);
                    }
                }
                try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            }
            if(moveTileList.size()>0){
                gameState.readyToMove=unit;
            }
        }
    }

    public static void attackHighlighting(ActorRef out,GameState gameState,Unit unit){
        if(UnitManagement.canAttack(unit)){
            List<Unit> attackRangeList=getAttackTarget(out, gameState, unit);
            for(Unit target:attackRangeList){
                Tile tile=BasicObjectBuilders.loadTile(target.getPosition().getTilex(), target.getPosition().getTiley());
                BasicCommands.drawTile(out, tile, 2);
            }
            if(attackRangeList.size()>0){
                gameState.readyToAttack=unit;
            }
        }else{
            BasicCommands.addPlayer1Notification(out, "this unit can not attack", 5);
            gameState.readyToAttack=null;
        }
    }

    public static void clearHighlighting(ActorRef out){
        for(int i=0;i<9;i++){
            for(int j=0;j<5;j++){
                Tile tile = BasicObjectBuilders.loadTile(i, j);
                BasicCommands.drawTile(out, tile, 0);
            }
        }
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void spellHighlighting(GameState gameState,ActorRef out){
        for(Unit unit:gameState.humanPlayerUnitList){
            Position unitPosition=unit.getPosition();
            int tilex=unitPosition.getTilex();
            int tiley=unitPosition.getTiley();
            Tile tile = BasicObjectBuilders.loadTile(tilex, tiley);
            BasicCommands.drawTile(out, tile, 1);
        }
    }

    public static void humanAvatarHighlighting(GameState gameState,ActorRef out){
        	Unit humanAvatar = gameState.humanPlayer ;
            Position unitPosition=humanAvatar.getPosition();
            int tilex=unitPosition.getTilex();
            int tiley=unitPosition.getTiley();
            Tile tile = BasicObjectBuilders.loadTile(tilex, tiley);
            BasicCommands.drawTile(out, tile, 1);
        }
    
    public static void exceptAvatarHighlighting(GameState gameState,ActorRef out,Unit unit){
        // Human avatar
        Unit humanAvatar = gameState.humanPlayer;
        List<Unit> unitList = gameState.humanPlayerUnitList;
        int Tilex = unit.getPosition().getTilex();
        int Tiley = unit.getPosition().getTiley();
        for (Unit u : unitList) {
            // Skip the human avatar : exclude it from highlighting
            if (u.equals(humanAvatar)) {
                continue;
            }
            int x = u.getPosition().getTilex();
            int y = u.getPosition().getTiley();
            if (x >= Tilex - 1 && x <= Tilex + 1 && y >= Tiley - 1 && y <= Tiley + 1) {
                Tile tile = BasicObjectBuilders.loadTile(x, y);
                BasicCommands.drawTile(out, tile, 1);
            }
        }
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void humanUnitHighlighting(GameState gameState,ActorRef out,Unit unit){
        //get the human's unit list
        List<Unit> unitList=gameState.humanPlayerUnitList;
        //highlight the human units
        int Tilex=unit.getPosition().getTilex();
        int Tiley=unit.getPosition().getTiley();
        for(Unit u:unitList){
            int x=u.getPosition().getTilex();
            int y=u.getPosition().getTiley();
            if(x>=Tilex-1 && x<=Tilex+1 && y>=Tiley-1 && y<=Tiley+1){
                Tile tile = BasicObjectBuilders.loadTile(x,y);
                BasicCommands.drawTile(out, tile, 2);
            }
        }
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

    }

    public static List<Tile> tileWithUnit(GameState gameState,ActorRef out){
        List<Unit> allUnitList =new ArrayList<>();
        allUnitList.addAll(gameState.humanPlayerUnitList);
        allUnitList.addAll(gameState.aiPlayerUnitList);

        List<Tile> tileWithUnit=new ArrayList<>();
        for(Unit unit:allUnitList){
            Tile tile = BasicObjectBuilders.loadTile(unit.getPosition().getTilex(),unit.getPosition().getTiley());
            tileWithUnit.add(tile);
        }
        return tileWithUnit;
    }

    public static List<Tile> tileWithoutUnit(GameState gameState,ActorRef out){
        List<Tile> tileWithUnitList =tileWithUnit(gameState, out);
        List<Tile> tileWithoutUnitList=new ArrayList<>();
        
        for(Tile tile:gameState.board){
            if(getUnitFromTile(gameState,out,tile)==null){
                tileWithoutUnitList.add(tile);
            }
        }
        return tileWithoutUnitList;
    }

    public static Unit getUnitFromTile(GameState gameState,ActorRef out,Tile tile){
        List<Unit> allUnitList=new ArrayList<>();
        allUnitList.addAll(gameState.humanPlayerUnitList);
        allUnitList.addAll(gameState.aiPlayerUnitList);

        for(Unit unit:allUnitList){
            if(unit.getPosition().getTilex()==tile.getTilex() && unit.getPosition().getTiley()==tile.getTiley()){
                return unit;
            }
        }
        return null;
    }

    public static boolean isStuck(Tile start,Tile end,ActorRef out,GameState gameState){
        int stepsX = Math.abs(end.getTilex() - start.getTilex());
        int stepsY = Math.abs(end.getTiley() - start.getTiley());
        int totalSteps = Math.max(stepsX, stepsY);

        int dx = Integer.compare(end.getTilex(), start.getTilex());
        int dy = Integer.compare(end.getTiley(), start.getTiley());

        int currentX = start.getTilex();
        int currentY = start.getTiley();

        if (totalSteps == 1) {
            if(stepsX!=0 && stepsY!=0){
                Tile horizontal=BasicObjectBuilders.loadTile(currentX+dx, currentY);
                Tile vertical=BasicObjectBuilders.loadTile(currentX, currentY+dy);
                if(getUnitFromTile(gameState, out, horizontal)!=null && getUnitFromTile(gameState, out, vertical)!=null){
                    return true;
                }
            }
            return false;
        }else{
            for(int i=1;i<totalSteps;i++){
                currentX += dx;
                currentY += dy;
                Tile t = BasicObjectBuilders.loadTile(currentX,currentY);
                if(getUnitFromTile(gameState, out, t)!=null){
                    return true;
                }
            }return false;
        }
    }

    public static List<Tile> getSummonTile(ActorRef out,GameState gameState,Unit unit){
        List<Tile> summonTileList=new ArrayList<>();
        int x=unit.getPosition().getTilex();
        int y=unit.getPosition().getTiley();
        int[][] possibleSummon = {
            {x-1,y-1},{x-1,y},{x-1,y+1},{x,y-1},{x,y+1},{x+1,y-1},{x+1,y},{x+1,y+1}
        };
        for (int[] summon : possibleSummon) {
            if(summon[0] >= 0 && summon[0] < 9 && summon[1] >= 0 && summon[1] < 5 ) {
                Tile tile = BasicObjectBuilders.loadTile(summon[0],summon[1]);
                if(Board.getUnitFromTile(gameState, out, tile)==null){
                    summonTileList.add(tile);
                }
            }
        }
        return summonTileList;
    }
    public static List<Tile> getMoveTile(ActorRef out,GameState gameState,Unit unit){
        List<Tile> moveTileList=new ArrayList<>();
        if(unit.getName()!=null && unit.getName().equals("Young Flamewing")){
            //flying,can move to any unoccupied space
            moveTileList.addAll(tileWithoutUnit(gameState, out));
        }else{
            int x=unit.getPosition().getTilex();
            int y=unit.getPosition().getTiley();
            Tile start = BasicObjectBuilders.loadTile(x,y);
            int[][] possibleMoves = {
                {x-2,y},{x-1,y-1},{x-1,y},{x-1,y+1},{x,y-2},{x,y-1},{x,y+1},{x,y+2},
                {x+1,y-1},{x+1,y},{x+1,y+1},{x+2,y}
            };
            for (int[] summon : possibleMoves) {
                if(summon[0] >= 0 && summon[0] < 9 && summon[1] >= 0 && summon[1] < 5 ) {
                    Tile end = BasicObjectBuilders.loadTile(summon[0],summon[1]);
                    moveTileList.add(end);
                }
            }
        }
        return moveTileList;
    }
    public static List<Unit> getAttackTarget(ActorRef out,GameState gameState,Unit unit){
        List<Unit> attackRangeList=new ArrayList<>();
        if(unit.isProvoked()){
            Tile tile=BasicObjectBuilders.loadTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
            List<Unit> attackList=UnitAbilities.provokeAttackList(gameState, out, tile);
            System.out.println(attackList.size());
            for(Unit attackTarget:attackList){
                attackRangeList.add(attackTarget);
            }
        }else{
            //if the unit is aiplayer's
            if(gameState.aiPlayerUnitList.contains(unit)){
                List<Unit> unitList=gameState.humanPlayerUnitList;
                int Tilex=unit.getPosition().getTilex();
                int Tiley=unit.getPosition().getTiley();
                for(Unit u:unitList){
                    int x=u.getPosition().getTilex();
                    int y=u.getPosition().getTiley();
                    if(x>=Tilex-1 && x<=Tilex+1 && y>=Tiley-1 && y<=Tiley+1){
                        attackRangeList.add(u);
                    }
                }
            }else{
                List<Unit> unitList=gameState.aiPlayerUnitList;
                int Tilex=unit.getPosition().getTilex();
                int Tiley=unit.getPosition().getTiley();
                for(Unit u:unitList){
                    int x=u.getPosition().getTilex();
                    int y=u.getPosition().getTiley();
                    if(x>=Tilex-1 && x<=Tilex+1 && y>=Tiley-1 && y<=Tiley+1){
                        attackRangeList.add(u);
                    }
                }
            }

        }
        return attackRangeList;
    }
}
