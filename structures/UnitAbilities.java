package structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.text.Style;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import commands.BasicCommands;
import events.TileClicked;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class UnitAbilities {



    public static void gainHealth(GameState gameState,ActorRef out,Tile tile,int healNumber){
        Unit unit=Board.getUnitFromTile(gameState,out,tile);
        if(unit!=null){
            int health=unit.getHealth()+healNumber;
            unit.setHealth(health);
            BasicCommands.setUnitHealth(out, unit, health);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        }
    }

    public static void gainAttack(GameState gameState,ActorRef out,Tile tile,int attackNumber){
        Unit unit=Board.getUnitFromTile(gameState,out,tile);
        if(unit!=null){
            int attack=unit.getAttack()+attackNumber;
            unit.setAttack(attack);
            BasicCommands.setUnitAttack(out, unit, attack);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        }
    }

    public static void damage(GameState gameState,ActorRef out,Unit unit,int damageNumber){
        int health=unit.getHealth();
        Unit player1_unit=TileClicked.isPlayer1Unit(gameState, unit.getPosition().getTilex(), unit.getPosition().getTiley());
        Unit player2_unit=TileClicked.isPlayer2Unit(gameState, unit.getPosition().getTilex(), unit.getPosition().getTiley());
        if(unit.getName()!=null && health-damageNumber>0){
            int new_health=unit.getHealth()-damageNumber;
            unit.setHealth(new_health);
            BasicCommands.setUnitHealth(out, unit, new_health);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        }else if(unit.getName()!=null && health-damageNumber<=0){
            unit.setHealth(0);
            BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.death);
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            BasicCommands.deleteUnit(out, unit);

            if(player1_unit!=null){
                gameState.humanPlayerUnitList.remove(unit);
            }else{
                gameState.aiPlayerUnitList.remove(unit);
            }
            UnitAbilities.deathwatch(gameState, out);
        }else if(unit.getName()==null && player1_unit!=null ){
            PlayerManagement.updatePlayer1Health(gameState, out, -damageNumber);
        }else if(unit.getName()==null && player2_unit!=null ){
            PlayerManagement.updatePlayer2Health(gameState, out, -damageNumber);
        }
    }
    
    
    public static void summonWraithlingBehind(GameState gameState,ActorRef out,Tile tile){
        Unit unit=Board.getUnitFromTile(gameState, out, tile);
        int tilex=unit.getPosition().getTilex();
        int tiley=unit.getPosition().getTiley();

        if(tilex>0){
            Tile summonTile=BasicObjectBuilders.loadTile(tilex-1, tiley);
            Unit checkUnit=Board.getUnitFromTile(gameState,out,summonTile);
            if(checkUnit==null){
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
        }else{
            System.out.println("its already has unit behind");
        }
    }

    public static List<Unit> provokeEnemyList(GameState gameState,ActorRef out,Tile tile){
        //check which player's unit
        Unit player1_unit=TileClicked.isPlayer1Unit(gameState, tile.getTilex(), tile.getTiley());
        Unit player2_unit=TileClicked.isPlayer2Unit(gameState, tile.getTilex(), tile.getTiley());
        //find the enemy unit on the adjacent square,store the unit 
        List<Unit> enemyUnitList=new ArrayList<>();
        if(player1_unit!=null){
            gameState.provokeUnit=player1_unit;
            for(Unit u:gameState.aiPlayerUnitList){
                int x=u.getPosition().getTilex();
                int y=u.getPosition().getTiley();
                if(x>=tile.getTilex()-1 && x<=tile.getTilex()+1 && y>=tile.getTiley()-1 && y<=tile.getTiley()+1){
                    enemyUnitList.add(u);
                }
            }
        }else if(player2_unit!=null){
            gameState.provokeUnit=player2_unit;
            for(Unit u:gameState.humanPlayerUnitList){
                int x=u.getPosition().getTilex();
                int y=u.getPosition().getTiley();
                if(x>=tile.getTilex()-1 && x<=tile.getTilex()+1 && y>=tile.getTiley()-1 && y<=tile.getTiley()+1){
                    enemyUnitList.add(u);
                }
            }
        }
        return enemyUnitList;
    }

    public static List<Unit> provokeAttackList(GameState gameState,ActorRef out,Tile tile){
        //check which player's unit
        Unit player1_unit=TileClicked.isPlayer1Unit(gameState, tile.getTilex(), tile.getTiley());
        Unit player2_unit=TileClicked.isPlayer2Unit(gameState, tile.getTilex(), tile.getTiley());
        //find the enemy unit with provoke
        List<Unit> attackUnitList=new ArrayList<>();
        if(player1_unit!=null){
            for(Unit u:gameState.aiPlayerUnitList){
                int x=u.getPosition().getTilex();
                int y=u.getPosition().getTiley();
                if(u.getName()!=null){
                    if(u.getName().equals("Swamp Entangler") || u.getName().equals("Ironcliff Guardian")){
                        if(x>=tile.getTilex()-1 && x<=tile.getTilex()+1 && y>=tile.getTiley()-1 && y<=tile.getTiley()+1){
                            attackUnitList.add(u);
                        }
                    }
                }
            }
        }else if(player2_unit!=null){
            for(Unit u:gameState.humanPlayerUnitList){
                int x=u.getPosition().getTilex();
                int y=u.getPosition().getTiley();
                if(u.getName()!=null){
                    if(u.getName().equals("Rock Pulveriser")){
                        if(x>=tile.getTilex()-1 && x<=tile.getTilex()+1 && y>=tile.getTiley()-1 && y<=tile.getTiley()+1){
                            attackUnitList.add(u);
                        }
                }
            }
        }
        return attackUnitList;
    }

    public static void destroyAdjacentUnit(GameState gameState,ActorRef out,Tile tile){
        //get the unit's health
        Unit unit = Board.getUnitFromTile(gameState,out,tile);
        int health=unit.getHealth();
        //get the enemy unit in an adjacent square,find the unit is below its maximum heath.
        int tilex=tile.getTilex();
        int tiley=tile.getTiley();

        int max_health=0;
        Unit target_Unit=null;
        for(Unit u:gameState.aiPlayerUnitList){
            int x=u.getPosition().getTilex();
            int y=u.getPosition().getTiley();
            if(x>=tilex-1 && x<=tilex+1 && y>=tiley-1 && y<=tiley+1){
                if(u.getHealth()<health && u.getHealth()>max_health){
                    max_health=u.getHealth();
                    target_Unit=u;
                }
            }
        }
        if(target_Unit!=null){
            //update the enemy health is 0 and remove from the board
            target_Unit.setHealth(0);
            BasicCommands.playUnitAnimation(out, target_Unit, UnitAnimationType.death);
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            BasicCommands.deleteUnit(out, target_Unit);
            gameState.aiPlayerUnitList.remove(target_Unit);
            UnitAbilities.deathwatch(gameState, out);
        }
    }

    public static void summonWraithlingByRandom(GameState gameState,ActorRef out,Tile tile){
        Unit unit=Board.getUnitFromTile(gameState,out,tile);
        int tilex=unit.getPosition().getTilex();
        int tiley=unit.getPosition().getTiley();

        List<Tile> target_tile = new ArrayList<>();
        for(int i=tilex-1;i<=tilex+1;i++){
            for(int j=tiley-1;j<=tiley+1;j++){
                Tile t=BasicObjectBuilders.loadTile(i, j);
                if(Board.getUnitFromTile(gameState,out,t)==null){
                    target_tile.add(t);
                }
            }
        }
        if(target_tile.size()>0){
            Random random=new Random();
            int n=random.nextInt(target_tile.size());
            Tile summonTile=target_tile.get(n);

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
            try {Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
        }

    }

    public static void gainEffectsAdjacentUnit(GameState gameState,ActorRef out,Tile tile){
        //check which player's unit
        Unit player1_unit=TileClicked.isPlayer1Unit(gameState, tile.getTilex(), tile.getTiley());
        Unit player2_unit=TileClicked.isPlayer2Unit(gameState, tile.getTilex(), tile.getTiley());

        if(player1_unit!=null){
            int tiley=player1_unit.getPosition().getTiley();
            int tilex=player1_unit.getPosition().getTilex();
            for(Unit u:gameState.humanPlayerUnitList){
                int y=u.getPosition().getTiley();
                int x=u.getPosition().getTilex();
                if(y==tiley && (x==tilex-1 || x==tilex+1)){
                    //gain +1 attack and +1 health
                    int health=u.getHealth()+1;
                    int attack=u.getAttack()+1;
                    u.setHealth(health);
                    u.setAttack(attack);
                    BasicCommands.setUnitHealth(out, u, health);
                    BasicCommands.setUnitAttack(out, u, attack);
                    try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }else if (player2_unit!=null) {
            int tiley=player2_unit.getPosition().getTiley();
            int tilex=player2_unit.getPosition().getTilex();
            System.out.println(tilex+","+tiley);
            for(Unit u:gameState.aiPlayerUnitList){
                int y=u.getPosition().getTiley();
                int x=u.getPosition().getTilex();
                if(y==tiley && (x==tilex-1 || x==tilex+1)){
                    //gain +1 attack and +1 health
                    int health=u.getHealth()+1;
                    int attack=u.getAttack()+1;
                    u.setHealth(health);
                    u.setAttack(attack);
                    BasicCommands.setUnitHealth(out, u, health);
                    BasicCommands.setUnitAttack(out, u, attack);
                    try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }
    }

    public static void Zeal(GameState gameState,ActorRef out,Unit unit){
        for(Unit u:gameState.aiPlayerUnitList){
            if(u.getName()!=null && u.getName().equals("Silverguard Knight")){
                Tile tile=BasicObjectBuilders.loadTile(u.getPosition().getTilex(), u.getPosition().getTiley());
                gainAttack(gameState, out, tile, 2);
            }
        }
    }

    public static void flying(GameState gameState,ActorRef out){
        List<Tile> moveTileList=Board.tileWithoutUnit(gameState, out);
        for(Tile t:moveTileList){
            BasicCommands.drawTile(out, t, 1);
            try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
        }
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void deathwatch(GameState gameState,ActorRef out){
        for(Unit unit:gameState.humanPlayerUnitList){
            if(unit.getName()!=null){
                Tile tile=BasicObjectBuilders.loadTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
                if (unit.getName().equals("Bad Omen")) {
                    UnitAbilities.gainAttack(gameState, out, tile, 1);
                } else if (unit.getName().equals("Shadow Watcher")) {
                    UnitAbilities.gainAttack(gameState, out, tile, 1);
                    UnitAbilities.gainHealth(gameState, out, tile, 1);
                } else if (unit.getName().equals("Bloodmoon Priestess")) {
                    UnitAbilities.summonWraithlingByRandom(gameState, out, tile);
                } else if (unit.getName().equals("Shadowdancer")) {
                    PlayerManagement.updatePlayer1Health(gameState, out, 1);
                    PlayerManagement.updatePlayer2Health(gameState, out, -1);
                }
            }
        }
    }
    public static void openingGambit(GameState gameState,ActorRef out,Tile tile){
        for(Unit unit:gameState.humanPlayerUnitList){
            if(unit.getName()!=null){
                if (unit.getName().equals("Gloom Chaser")) {
                    UnitAbilities.summonWraithlingBehind(gameState, out, tile);
                } else if (unit.getName().equals("Nightsorrow Assassin")) {
                    UnitAbilities.destroyAdjacentUnit(gameState, out, tile);
                }
            }
        }
    }

} 