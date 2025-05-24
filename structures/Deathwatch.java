package structures;

import akka.actor.ActorRef;
import structures.GameState;
import structures.UnitManagement;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;

/*
Triggers the Deathwatch ability for different creatures
Bad omens - gains +1 attack
Bloodmoon Priestess - Summons a wraithling on random adjacent tile
ShadowWatcher - gains +1 attack +1 health
Shadowdancer - deals 1 damage to enemy avatar + heals itself +1
 */


public class Deathwatch implements AbilityTrigger {
    @Override
    public void trigger(Unit source, Unit target, GameState gameState, ActorRef out) {

        if (target != null && target.getHealth() <=0) {
            String sourceName = source.getName();

            int tx = source.getPosition().getTilex();
            int ty = source.getPosition().getTiley();
            Tile sourceTile = BasicObjectBuilders.loadTile(tx, ty);

            if (sourceName.equals("Bad Omen")) {
                UnitAbilities.gainAttack(gameState, out, sourceTile, 1);
            } else if (sourceName.equals("Shadow Watcher")) {
                UnitAbilities.gainAttack(gameState, out, sourceTile, 1);
                UnitAbilities.gainHealth(gameState, out, sourceTile, 1);
            } else if (sourceName.equals("Bloodmoon Priestess")) {
                UnitAbilities.summonWraithlingByRandom(gameState, out, sourceTile);
            } else if (sourceName.equals("Shadowdancer")) {
                UnitAbilities.gainHealth(gameState, out, sourceTile, 1);
            }
        }
    }
}
