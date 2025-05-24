package structures;

import akka.actor.ActorRef;
import utils.*;
import java.util.*;
import structures.basic.*;
import structures.UnitManagement;
import commands.BasicCommands;
import events.TileClicked;

public class AICompute {

    private static List<Card> availableCards=null;
    private static Map<Unit,Tile> bestMoveableTileList=new HashMap<>();
    private static Map<Unit,Unit> bestTargetList=new HashMap<>();

    public static void aiPlay(ActorRef out, GameState gameState){
        try {Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
        getPossibleAiCards(gameState);
        summonTile(out, gameState);
        try {Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
        moveAndAttack(out, gameState);
        TurnManagement.startTurn(out, gameState);
        try {Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void getPossibleAiCards(GameState gameState){
        availableCards=new ArrayList<>();
        // 1.get the card that has enough mana
        for(Card card: gameState.aiPlayerCardList){
            if(card.getManacost() <= PlayerManagement.getAiPlayer().getMana()){
                availableCards.add(card);
            }
        }
        // 2. order by priority, rush ability> health > spell
        availableCards.sort((c1,c2)->{
            int priority1=getCardPriority(c1);
            int priority2=getCardPriority(c2);
            return Integer.compare(priority2, priority1); 
        });
        // return availableCardList;
    }
    public static int getCardPriority(Card card){
        if(card.getCardname().equals("Saberspine Tiger")){
            return 100; // unit with rush can move to anywhere
        }else if(card.getCardname().equals("Ironcliff Guardian")){
            return (TurnManagement.getTurnNumber() > 5) ? 90 : 50; // the health of this unit is 10, we can not summon in the beginning of game
        }else if(card.isCreature()){
            return 60; 
        }else if(card.getCardname().equals("Truestrike")){
            return 60; //the spell ability can deal 2 damange to enemy
        }else if(card.getCardname().equals("Beamshock")){
            return 50; // the target can not move and attack next turn
        }else{
            return 40;
        }
    }
    public static void summonTile(ActorRef out,GameState gameState){
        Tile bestTile=null;
        Card bestCard=null;
        float score=0;
        for(Card card:availableCards){
            LinkedHashSet<Tile> summonTiles=getPossibleSummonTiles(out, gameState, card);
            if(card.isCreature()){
                for(Tile tile:summonTiles){
                    float tile_score=getCreatureSummonTileScore(out, gameState, tile, card);
                    if(tile_score<score){
                        score=tile_score;
                        bestCard=card;
                        bestTile=tile;
                    }
                }
            }else{
                for(Tile tile:summonTiles){
                    float tile_score=getSpellSummonTileScore(out, gameState, tile, card);
                    if(tile_score<score){
                        score=tile_score;
                        bestCard=card;
                        bestTile=tile;
                    }
                }
            }
        }
        System.out.println("summon: "+score);
        System.out.println(bestCard.getCardname());
        System.out.println(bestTile.getTilex()+" , "+bestTile.getTiley());
        if(bestCard!=null && bestTile!=null){
            if(bestCard.isCreature()){
                drawUnit(out, gameState, bestTile, bestCard);
            }else{
                playSpell(out, gameState, bestTile, bestCard);
            }
        }
    }
    public static void moveAndAttack(ActorRef out,GameState gameState){
        float overall=0;
        Unit attacker=null;
        for(Unit unit:gameState.aiPlayerUnitList){
            float overallScore=evaluateAttack(unit, out, gameState);
            System.out.println(overallScore);
            if(overallScore>overall){
                overall=overallScore;
                attacker=unit;
            }
        }
        if(attacker!=null){
            // get the target tile of attacker
            Tile bestTile=bestMoveableTileList.get(attacker);
            Unit bestTarget=bestTargetList.get(attacker);
            if(bestTarget==null){
                //only move
                System.out.println("attacker: "+attacker.getPosition().getTilex()+","+attacker.getPosition().getTiley());
                System.out.println("moveableTile: "+bestTile.getTilex()+","+bestTile.getTiley());
                UnitManagement.move(out, gameState, bestTile, attacker);
            }else{
                System.out.println("attacker: "+attacker.getPosition().getTilex()+","+attacker.getPosition().getTiley());
                System.out.println("target: "+bestTarget.getPosition().getTilex()+","+bestTarget.getPosition().getTiley());
                System.out.println("moveableTile: "+bestTile.getTilex()+","+bestTile.getTiley());

                Tile attackerTile=BasicObjectBuilders.loadTile(attacker.getPosition().getTilex(), attacker.getPosition().getTiley());
                Tile targetTile=BasicObjectBuilders.loadTile(bestTarget.getPosition().getTilex(), bestTarget.getPosition().getTiley());
                int dx=Math.abs(Integer.compare(targetTile.getTilex(), attackerTile.getTilex()));
                int dy=Math.abs(Integer.compare(targetTile.getTiley(), attackerTile.getTiley()));
                int totalSteps=Math.max(dx, dy);
                if(totalSteps==1){
                    //only attack
                    UnitManagement.attack(out, gameState, attacker, bestTarget);
                }else{
                    //move and attack
                    UnitManagement.move(out, gameState, bestTile, attacker);
                    UnitManagement.attack(out, gameState, attacker, bestTarget);
                }
            }
        }
    }
    public static float evaluateAttack(Unit attacker,ActorRef out,GameState gameState){
        List<Tile> moveabTiles=new ArrayList<>();
        Tile attackerTile=BasicObjectBuilders.loadTile(attacker.getPosition().getTilex(),attacker.getPosition().getTiley());
        //1. check the attacker can move,get the tile of attacker
        if(UnitManagement.canMove(attacker)){
            moveabTiles=Board.getMoveTile(out, gameState, attacker);
            moveabTiles.add(attackerTile);
        }
        
        Tile bestTile=null;
        Unit bestTarget=null;
        float overall=0;
        for(Tile tile:moveabTiles){
            if(!Board.isStuck(attackerTile, tile, out, gameState) && Board.getUnitFromTile(gameState, out, tile)==null){
                //get the score of move
                float moveScore=getMoveScore(out, gameState, tile);
                // get the score of attack
                List<Unit> targetList=getAttackTarget(out, gameState,tile);
                //2.calculate the score of each target and choose the best one
                Unit bestTargetUnit=null;
                float attackScore=0;
                for(Unit unit:targetList){
                    float targetScore=getAttackScore(out, gameState, unit, attacker);
                    if(targetScore>attackScore){
                        bestTargetUnit=unit;
                        attackScore=targetScore;
                    }
                }
                //calcualte the overall score
                float overallScore=moveScore*0.6f+attackScore*0.4f;
                if(overallScore>overall){
                    bestTarget=bestTargetUnit;
                    bestTile=tile;
                    overall=overallScore;
                }
            }
        }
        bestMoveableTileList.put(attacker, bestTile);
        bestTargetList.put(attacker, bestTarget);
        return overall;
    }
    public static float getMoveScore(ActorRef out,GameState gameState,Tile tile){
        float score=0;
        //1.the distance between key enemy unit(avatar+attack>3)
        int avatarScore=0;
        Position humanPlayer=gameState.humanPlayer.getPosition();
        int distanceToAvatar = Math.abs(tile.getTilex() - humanPlayer.getTilex()) 
                      + Math.abs(tile.getTiley() - humanPlayer.getTiley());
        int highThreatCount=0;
        for(Unit unit:gameState.humanPlayerUnitList){
            if(unit.getAttack()>3){
                highThreatCount++;
            }
        }
        avatarScore+=(10-distanceToAvatar)*2+highThreatCount;
        //2.distance to center
        int centerScore=0;
        if(tile.getTilex()>=4 && tile.getTilex()<=6){
            centerScore+=3;
        }
        //3.get the count of unit with heal ability or low health
        int defensiveScore=0;
        for(Unit unit:gameState.aiPlayerUnitList){
            if(unit.getHealth()<5){
                defensiveScore+=5;
            }
        }
        //4.if is under threat
        int safeScore=0;
        if(isUnderThreat(out, gameState, tile)){
            safeScore-=40;
        }
        score=avatarScore*50+30*centerScore+20*defensiveScore+safeScore;
        return score;
    }
    public static float getAttackScore(ActorRef out,GameState gameState,Unit targetUnit,Unit attacker){
        float score=0;
        // 1.check the threat
        score+=targetUnit.getAttack()*2;
        score+=(20 - targetUnit.getHealth()) * 1.5f;
        // 2.
        if(targetUnit.getName()==null && TileClicked.isPlayer1Unit(gameState, targetUnit.getPosition().getTilex(), targetUnit.getPosition().getTiley())!=null){
            score*=3;
        }
        // 3.
        int attackDamage=Math.min(attacker.getAttack(),targetUnit.getHealth());
        int counterDamage=targetUnit.getHealth() > 0 ? targetUnit.getAttack() : 0;
        score+=(attackDamage-counterDamage)*2;
        return score;
    }
    public static float getCreatureSummonTileScore(ActorRef out,GameState gameState,Tile tile,Card card){
        //check if under the attack range
        int safeScore=0;
        if(isUnderThreat(out, gameState, tile)){
            safeScore+=100;
        }else{
            safeScore+=0;
        }
        // check the distance between humanPlayer's avatar
        int avatarScore=0;
        Position humanPlayer=gameState.humanPlayer.getPosition();
        int distance = Math.abs(tile.getTilex() - humanPlayer.getTilex()) 
                      + Math.abs(tile.getTiley() - humanPlayer.getTiley());
        avatarScore+=distance*10;
        // rush need to close to enemy
        int abilityScore=0;
        if(card.hasRush()){
            int d = distanceOfClosestEnemy(out, gameState, tile);
            abilityScore+=d*5;
        }else if(card.getCardname().equals("Swamp Entangler") || card.getCardname().equals("Silverguard Knight") || card.getCardname().equals("Ironcliff Guardian")){
            abilityScore+=tile.getTilex()*20;
        }
        // distance to center
        int centerScore=0;
        int distanceToCenter = Math.abs(tile.getTilex() - 4) + Math.abs(tile.getTiley() - 2);
        centerScore+=distanceToCenter*15;
        return -(float) (safeScore*0.4+avatarScore*0.3+abilityScore*0.2+centerScore*0.1);
    }
    public static float getSpellSummonTileScore(ActorRef out,GameState gameState,Tile tile,Card card){
        Unit targetUnit=Board.getUnitFromTile(gameState, out, tile);
        int score=0;
        if(targetUnit.getName()!=null){
            if(card.getCardname().equals("Sundrop Elixir")){
                score-=20-targetUnit.getHealth();
                // if unit has provoke
                if(targetUnit.getName().equals("Swamp Entangler") || targetUnit.getName().equals("Silverguard Knight") || targetUnit.getName().equals("Ironcliff Guardian")){
                    score-=30;
                }else if(targetUnit.getAttack()>3){
                    score-=20;
                }
            }else if(card.getCardname().equals("Truestrike")){
                score = (targetUnit.getHealth() - 2) * 20;
                if(targetUnit.getAttack()>=3){
                    score-=50;
                }else if(targetUnit.hasRush()){
                    score-=30;
                }
            }else if(card.getCardname().equals("Beamshock")){
                score = targetUnit.getAttack() * 15 + targetUnit.getHealth() * 5;
            }
        }
        return score;
    }
    public static LinkedHashSet<Tile> getPossibleSummonTiles(ActorRef out,GameState gameState,Card card){
        LinkedHashSet<Tile> summonTileList= new LinkedHashSet<>();
        if(card.isCreature()){
            //find all ai player's unit's summon tiles
            for(Unit unit:gameState.aiPlayerUnitList){
                summonTileList.addAll(getSummonTile(out,gameState,unit));
            }
        }else if(card.getCardname().equals("Sundrop Elixir")){
            //get all friendly unit tile
            for(Unit u:gameState.aiPlayerUnitList){
                Tile t=BasicObjectBuilders.loadTile(u.getPosition().getTilex(), u.getPosition().getTiley());
                summonTileList.add(t);
            }
        }else if(card.getCardname().equals("Truestrike")){
            //get enemy unit tile
            for(Unit u:gameState.humanPlayerUnitList){
                Tile t=BasicObjectBuilders.loadTile(u.getPosition().getTilex(), u.getPosition().getTiley());
                summonTileList.add(t);
            }
        }else if(card.getCardname().equals("Beamshock")){
            //get enemy unit tile without avatar
            for(Unit u:gameState.humanPlayerUnitList){
                if(u.getName()==null){
                    Tile t=BasicObjectBuilders.loadTile(u.getPosition().getTilex(), u.getPosition().getTiley());
                    summonTileList.add(t);
                }
            }
        }
        return summonTileList;
    }
    public static boolean isUnderThreat(ActorRef out,GameState gameState,Tile targetTile){
        // get all enemies
        List<Unit> enemies=gameState.humanPlayerUnitList;
        Set<Tile> attackRangeList=new LinkedHashSet<>();
        // get the attack range of each enemy unit
        for(Unit unit:enemies){
            List<Tile> moveableTiles=Board.getMoveTile(out, gameState, unit);
            Tile unitTile=BasicObjectBuilders.loadTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
            for(Tile tile:moveableTiles){
                if(!Board.isStuck(unitTile, tile, out, gameState) && Board.getUnitFromTile(gameState, out, tile)==null){
                    List<Tile> attackRange=getAttackTile(out, gameState, tile);
                    attackRangeList.addAll(attackRange);
                }
            }
        }
        if(attackRangeList.contains(targetTile)){
            return true;
        }
        return false;
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
    public static List<Tile> getAttackTile(ActorRef out,GameState gameState,Tile tile){
        Unit unit=Board.getUnitFromTile(gameState, out, tile);
        List<Tile> attackRangeList=new ArrayList<>();
        if(unit!=null && unit.isProvoked()){
            List<Unit> attackList=UnitAbilities.provokeAttackList(gameState, out, tile);
            for(Unit attackTarget:attackList){
                Tile unitTile=BasicObjectBuilders.loadTile(attackTarget.getPosition().getTilex(), attackTarget.getPosition().getTiley());
                attackRangeList.add(unitTile);
            }
        }else{
            List<Unit> unitList=gameState.humanPlayerUnitList;
            int Tilex=tile.getTilex();
            int Tiley=tile.getTiley();
            for(Unit u:unitList){
                int x=u.getPosition().getTilex();
                int y=u.getPosition().getTiley();
                if(x>=Tilex-1 && x<=Tilex+1 && y>=Tiley-1 && y<=Tiley+1){
                    attackRangeList.add(BasicObjectBuilders.loadTile(x, y));
                }
            }
        }
        return attackRangeList;
    }
    public static List<Unit> getAttackTarget(ActorRef out,GameState gameState,Tile attackerTile){
        List<Unit> targetList=new ArrayList<>();
        List<Tile> attackRangeList=new ArrayList<>();
        Unit attacker=Board.getUnitFromTile(gameState, out, attackerTile);
        if(attacker!=null && UnitManagement.canAttack(attacker)){
            List<Tile> moveableTiles=Board.getMoveTile(out, gameState, attacker);

            for(Tile tile:moveableTiles){
                if(!Board.isStuck(attackerTile, tile, out, gameState) && Board.getUnitFromTile(gameState, out, tile)==null){
                    List<Tile> attackRange=getAttackTile(out, gameState, tile);
                    attackRangeList.addAll(attackRange);
                }
            }
            for(Unit u:gameState.humanPlayerUnitList){
                Tile enemy=BasicObjectBuilders.loadTile(u.getPosition().getTilex(), u.getPosition().getTiley());
                if(attackRangeList.contains(enemy)){
                    targetList.add(u);
                }
            }
        }
        return targetList;
    }
    public static int distanceOfClosestEnemy(ActorRef out,GameState gameState,Tile tile){
        List<Unit> enemies=gameState.humanPlayerUnitList;
        HashMap<Unit,Integer> enemy=new HashMap<>();
        for(Unit unit:enemies){
            int distance=Math.abs(tile.getTilex() - unit.getPosition().getTilex())+Math.abs(tile.getTiley() - unit.getPosition().getTiley());
            if(distance>0){
                enemy.put(unit, distance);
            }
        }
        int minValue=Collections.min(enemy.values());
        return minValue;
    }
    public static void drawUnit(ActorRef out,GameState gameState,Tile tile,Card card){
		if(card.isCreature()==true){
			//create unit and set the health and attack of the unit
			Unit unit = BasicObjectBuilders.loadUnit(card.getUnitConfig(),20+card.getId(), Unit.class);
			unit.setPositionByTile(tile);
			BasicCommands.drawUnit(out, unit, tile);
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
			UnitManagement.UnitIntialize(out, card, unit);
            gameState.aiPlayerUnitList.add(unit);

            abilities(out,gameState);

			//reduce the mana
			Player aiPlayer=PlayerManagement.getAiPlayer();
			int mana=aiPlayer.getMana()-card.getManacost();
			PlayerManagement.setPlayer2Mana(out,mana);

			//remove the card from list
			for(int i=0;i<gameState.aiPlayerCardList.size();i++){
                if(card.getId()==gameState.aiPlayerCardList.get(i).getId()){
                    gameState.aiPlayerCardList.remove(i);
                }
            }
		}
    }
    public static void playSpell(ActorRef out,GameState gameState,Tile tile,Card card){
        System.out.println(card.getCardname());
        Unit targetUnit=Board.getUnitFromTile(gameState, out, tile);
        if(card.getCardname().equals("Sundrop Elixir")){
            if(targetUnit.getHealth()+4>20){
                targetUnit.setHealth(20);
                BasicCommands.addPlayer1Notification(out, "play Sundrop Elixir", 5);
                BasicCommands.setUnitHealth(out, targetUnit, 20);
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            }else{
                BasicCommands.addPlayer1Notification(out, "play Sundrop Elixir", 5);
                UnitAbilities.gainHealth(gameState, out, tile, 4);
            }
        }else if(card.getCardname().equals("Truestrike")){
            BasicCommands.addPlayer1Notification(out, "play Truestrike", 5);
            UnitAbilities.damage(gameState, out, targetUnit, 2);
        }else if(card.getCardname().equals("Beamshock")){
            targetUnit.setStun(true);
            targetUnit.setStunDuration(2);
        }

        //reduce the mana
        Player aiPlayer=PlayerManagement.getAiPlayer();
        int mana=aiPlayer.getMana()-card.getManacost();
        PlayerManagement.setPlayer2Mana(out,mana);

        //remove the card from list
        for(int i=0;i<gameState.aiPlayerCardList.size();i++){
            if(card.getId()==gameState.aiPlayerCardList.get(i).getId()){
                gameState.aiPlayerCardList.remove(i);
            }
        }
    }
    public static void abilities(ActorRef out,GameState gameState){
        //opening gambit
        boolean hasOpeningGambit=false;
        for(Unit unit:gameState.aiPlayerUnitList){
            if(unit.getName()!=null && unit.getName().equals("Silverguard Squire")){
                hasOpeningGambit=true;
            }
        }
        if(hasOpeningGambit){
            Tile tile=BasicObjectBuilders.loadTile(gameState.aiPlayer.getPosition().getTilex(), gameState.aiPlayer.getPosition().getTiley());
            UnitAbilities.gainEffectsAdjacentUnit(gameState, out, tile);
        }
    }

}

