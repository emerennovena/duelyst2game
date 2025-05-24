package structures;

import akka.actor.ActorRef;
import utils.*;
import java.util.*;
import structures.basic.*;
import structures.UnitManagement;
import commands.BasicCommands;

public class AIPlayer {
    private static List<Card> cardList=OrderedCardLoader.getPlayer2Cards(2);
    private static Set<Card> summonedCard=new HashSet<>();

    public static void aiPlay(ActorRef out, GameState gameState) {
        Random random=new Random();
        boolean summonNewUnit = random.nextBoolean(); //50% chance to summon a unit or not
        List<Unit> enemyUnits=getAdjacentEnemies(gameState, out);
        boolean enemiesExist=!enemyUnits.isEmpty();
        boolean unitMoved=false;
        boolean unitAttacked=false;

        //if a unit is summoned
        if(summonNewUnit && !unitMoved && !unitAttacked){
        List<Card> availableCards=getPossibleAiCards(gameState);
            if (!availableCards.isEmpty()) {
                List<Tile> summonTiles=getPossibleSummonTiles(gameState, out);
                if (!summonTiles.isEmpty()) {
                    Card randomCard=getRandomCard(availableCards);
                    Tile randomSummon=getRandomSummonTile(summonTiles);
                    Unit summonedUnit=summonAIUnit(out, randomCard, randomSummon, gameState);
                    if (enemiesExist) {
                        aiMoveAndAttack(out,gameState,summonedUnit); //move and attack if there are enemies
                        unitMoved=true;
                        unitAttacked=true;
                        gameState.isMoved=1;
                        gameState.isAttacked=1;
                    }else{
                        moveAIUnit(out,gameState,randomSummon); //move if no enemies to attack
                        unitMoved=true;
                        unitAttacked=false;
                        gameState.isMoved=1;
                    }
                }
            }
        }

        //if a unit is not summoned, move a unit from existing units on the board
        if (!unitMoved && !unitAttacked) {
            List<Unit> aiUnits=gameState.aiPlayerUnitList;
            for (Unit aiUnit:aiUnits) {
                unitMoved=false;
                unitAttacked=false;
                gameState.isMoved=0;
                gameState.isAttacked=0;
                Random random2=new Random();
                Unit chosenUnit=aiUnits.get(random2.nextInt(aiUnits.size()));
                if (!unitMoved && !unitAttacked) {
                    if (enemiesExist) {
                        aiMoveAndAttack(out,gameState,chosenUnit);
                        unitMoved=true;
                        unitAttacked=true;
                        gameState.isMoved=1;
                        gameState.isAttacked=1;
                    }else{
                        List<Tile> moveTiles=getPossibleMoves(gameState, out);
                        Position unitPosition=chosenUnit.getPosition();
                        Tile randomMoveTile=getRandomMoveTile(moveTiles, BasicObjectBuilders.loadTile(unitPosition.getTilex(), unitPosition.getTiley()));
                        if (randomMoveTile!=null) {
                            moveAIUnit(out,gameState,randomMoveTile);
                            unitMoved=true;
                            unitAttacked=false;
                            gameState.isMoved=1;
                        }
                    }
                }
                if (unitMoved && unitAttacked) break;
            }
        }
    }

//---------------------------------------------------------get possible cards,summon,move,attack--------------------------------------------------------------
    public static List<Card> getPossibleAiCards(GameState gameState){
        List<Card> availableCards = new ArrayList<Card>();
        for(Card card: cardList){
            if(card.getIsCreature() && card.getManacost() <= PlayerManagement.getAiPlayer().getMana()){
                if(!summonedCard.contains(card)){
                    availableCards.add(card);
                }
            }
        }
        return availableCards;
    }

    public static List<Tile> getPossibleSummonTiles(GameState gameState, ActorRef out){
        List<Tile> summonTiles=new ArrayList<>();
        for(Unit aiUnit:gameState.aiPlayerUnitList){
        Position aiPos=aiUnit.getPosition();
        int x=aiPos.getTilex();
        int y=aiPos.getTiley();
        int[][] directions={{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1},{-1,1},{1,1}};
        for(int[] direction:directions){
            int newX=x+direction[0];
            int newY=y+direction[1];
            Tile tile=BasicObjectBuilders.loadTile(newX, newY);
            if(isValidTile(newX, newY) && Board.getUnitFromTile(gameState, out, tile)==null && !isAIUnitOnTile(gameState,newX,newY)){
                summonTiles.add(BasicObjectBuilders.loadTile(newX,newY));
            }
        }
        }
        return summonTiles;
    }

    public static List<Tile> getPossibleMoves(GameState gameState, ActorRef out){
        List<Tile> moveTiles=new ArrayList<>();
        Position aiPos=gameState.aiPlayer.getPosition();
        int x=aiPos.getTilex();
        int y=aiPos.getTiley();
        int[][] directions={{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1},{-1,1},{1,1},{-2,0},{2,0},{0,-2},{0,2}};
        for(int[] direction:directions){
            int newX=x+direction[0];
            int newY=y+direction[1];
            Tile tile=BasicObjectBuilders.loadTile(newX, newY);
            if(isValidTile(newX, newY) && Board.getUnitFromTile(gameState, out, BasicObjectBuilders.loadTile(newX,newY))==null && !isAIUnitOnTile(gameState,newX,newY)){
                moveTiles.add(BasicObjectBuilders.loadTile(newX,newY));
            }
        }
        return moveTiles;
    }

   public static List<Tile> getPossibleAttackTiles(GameState gameState, ActorRef out){
        List<Tile> attackTiles=new ArrayList<>();
        for(Unit aiUnit:gameState.aiPlayerUnitList){
            int x=aiUnit.getPosition().getTilex();
            int y=aiUnit.getPosition().getTiley();
            int[][] attackDirections={{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1},{-1,1},{1,1}};
            for(int[] attackDirection:attackDirections){
                int newX=x+attackDirection[0];
                int newY=y+attackDirection[1];
                if(isValidTile(newX, newY)){
                    for(Unit humanUnit:gameState.humanPlayerUnitList){
                        if(humanUnit.getPosition().getTilex()==newX && humanUnit.getPosition().getTiley()==newY && !isAIUnitOnTile(gameState,newX,newY)){
                            attackTiles.add(BasicObjectBuilders.loadTile(newX,newY));
                            break;
                        }
                    }
                }
            }
        }
        return attackTiles;
    }

   //--------------------------------------------------------get random card,summon,move,attack-----------------------------------------------------------------
   private static Card getRandomCard(List<Card> availableCards){
        if(!availableCards.isEmpty()){
            Random random=new Random();
            return availableCards.get(random.nextInt(availableCards.size()));
        }
        return null;
   }

   private static Tile getRandomSummonTile(List<Tile> summonTiles){
        if(!summonTiles.isEmpty()){
            Random random=new Random();
            return summonTiles.get(random.nextInt(summonTiles.size()));

        }
        return null;
   }

   private static Tile getRandomMoveTile(List<Tile> moveTiles, Tile currentTile){
    List<Tile> validMoveTiles = new ArrayList<>();
    for (Tile moveTile : moveTiles) {
        // Check if the move tile is adjacent
        if (Math.abs(moveTile.getTilex()-currentTile.getTilex())<= 1 && Math.abs(moveTile.getTiley()-currentTile.getTiley())<=1){
            validMoveTiles.add(moveTile);
        }
    }
        if(!validMoveTiles.isEmpty()){
            Random random=new Random();
            return validMoveTiles.get(random.nextInt(validMoveTiles.size()));
        }
        return null;
   }

   private static Tile getRandomAttackTile(List<Tile> attackTiles){
        if(!attackTiles.isEmpty()){
            Random random=new Random();
            return attackTiles.get(random.nextInt(attackTiles.size()));
        }
        return null;
   }

//----------------------------------------------------------------summon,move,attack--------------------------------------------------------------------
    private static Unit summonAIUnit(ActorRef out, Card card, Tile tile, GameState gameState){
        if(card.isCreature() && !summonedCard.contains(card)){
            summonedCard.add(card);
            Unit unit=BasicObjectBuilders.loadUnit(card.getUnitConfig(), card.getId(), Unit.class);
            unit.setPositionByTile(tile);
            BasicCommands.drawUnit(out, unit, tile);

            BasicCommands.setUnitHealth(out,unit,unit.getHealth());
            BasicCommands.setUnitAttack(out,unit,unit.getAttack());
            try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

		    UnitManagement.UnitIntialize(out, card, unit);
            gameState.aiPlayerUnitList.add(unit);
            gameState.readyToMove=unit;
            gameState.isMoved=0;
            gameState.isAttacked=0;

            Player aiPlayer=PlayerManagement.getAiPlayer();
            int newMana=aiPlayer.getMana()-card.getManacost();
            aiPlayer.setMana(newMana);
            PlayerManagement.setPlayer2Mana(out, newMana);
            try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
            return unit;
        }
        return null;
    }

    private static void moveAIUnit(ActorRef out, GameState gameState, Tile tile){
        Unit unit=gameState.readyToMove;
        BasicCommands.playUnitAnimation(out,unit,UnitAnimationType.move);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        BasicCommands.moveUnitToTile(out,unit,tile);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        unit.setPositionByTile(tile);
    }

    private static void aiMoveAndAttack(ActorRef out, GameState gameState, Unit aiUnit) {
        gameState.readyToMove=aiUnit;
        Position unitPosition=aiUnit.getPosition();
        List<Tile> moveTiles=getPossibleMoves(gameState, out);
        Tile moveTile=getRandomMoveTile(moveTiles, BasicObjectBuilders.loadTile(unitPosition.getTilex(), unitPosition.getTiley()));
        List<Unit> adjacentEnemies=getAdjacentEnemies(gameState, out);
        if (!adjacentEnemies.isEmpty()) {
            if (moveTile!=null) {
                moveAIUnit(out, gameState, moveTile);
                gameState.isMoved=1;
            }
        }
        if (!adjacentEnemies.isEmpty()) {
            Unit target=adjacentEnemies.get(new Random().nextInt(adjacentEnemies.size()));
            if (isAdjacent(aiUnit,target)) {
                BasicCommands.playUnitAnimation(out, aiUnit, UnitAnimationType.attack);
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
                target.setHealth(target.getHealth()-aiUnit.getAttack());
                BasicCommands.setUnitHealth(out, target, target.getHealth());
                UnitAbilities.damage(gameState, out, target, target.getAttack());
                gameState.isAttacked = 1;
                gameState.isMoved = 1;
                gameState.readyToMove = null;
            } else {
                if (moveTile!=null) {
                    moveAIUnit(out, gameState, moveTile);
                    gameState.isMoved = 1;
                }
            }
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

//----------------------------------------------------------abilities when attacking-------------------------------------------------------------------
//     private static void triggerAbilitiesOnAttack(Card card, Unit source, Unit target, GameState gameState, ActorRef out){
//         Provoke provoke=new Provoke();
//         if(card.getCardname().equals("Swamp Entangler") ||
//            card.getCardname().equals("Silverguard Knight") ||
//            card.getCardname().equals("Ironcliff Guardian")){
//            provoke.trigger(source,target,gameState,out);
//         }
//     }

//------------------------------------------------------------------extra methods-----------------------------------------------------------------------
    private static List<Unit> getAdjacentEnemies(GameState gameState, ActorRef out){
        List<Unit> adjacentEnemies=new ArrayList<>();
        for(Unit aiUnit:gameState.aiPlayerUnitList){
            Position aiPos=aiUnit.getPosition();
            int x=aiPos.getTilex();
            int y=aiPos.getTiley();
            int[][] directions={{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1},{-1,1},{1,1}};
            for(int[] direction:directions){
            int newX=x+direction[0];
            int newY=y+direction[1];
                if(isValidTile(newX,newY)){
                    for(Unit enemyUnit:gameState.humanPlayerUnitList){
                        if(enemyUnit.getPosition().getTilex()==newX && enemyUnit.getPosition().getTiley()==newY){
                            adjacentEnemies.add(enemyUnit);
                            break;
                        }
                    }
                }
            }
        }
        return adjacentEnemies;
    }

    private static boolean isAdjacent(Unit aiUnit, Unit target) {
        int aiX = aiUnit.getPosition().getTilex();
        int aiY = aiUnit.getPosition().getTiley();
        int targetX = target.getPosition().getTilex();
        int targetY = target.getPosition().getTiley();
        return Math.abs(aiX - targetX) <= 1 && Math.abs(aiY - targetY) <= 1;
    }

    private static boolean isValidTile(int x, int y){
        return x>=0 && x<9 && y>=0 && y<5;
    }

    private static boolean isAIUnitOnTile(GameState gameState, int x, int y){
        for(Unit aiUnit:gameState.aiPlayerUnitList){
            if(aiUnit.getPosition().getTilex()==x && aiUnit.getPosition().getTiley()==y){
                return true;
            }
        }
        return false;
    }

}
