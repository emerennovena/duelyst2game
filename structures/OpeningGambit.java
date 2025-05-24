package structures;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;

/*
Opening Gambit trigger for different creatures
Gloom Chaser - wraithling summoned directly behind
Nightsorrow assassin - destroys an adjacent enemy unit
Silverguard squire - gives adjacent allied unit +1 attack and health
 */

public class OpeningGambit implements AbilityTrigger {

    @Override
    public void trigger(Unit source, Unit target, GameState gameState,ActorRef out) {
        int tx = source.getPosition().getTilex();
        int ty = source.getPosition().getTiley();
        Tile sourceTile = BasicObjectBuilders.loadTile(tx, ty);
        String sourceName = source.getName();

        if (sourceName.equals("Gloom Chaser")) {
            UnitAbilities.summonWraithlingBehind(gameState, out, sourceTile);
            return;
        } else if (sourceName.equals("Nightsorrow Assassin")) {
            UnitAbilities.destroyAdjacentUnit(gameState, out, sourceTile);
            return;
        } else if (sourceName.equals("Silverguard Squire")) {
            UnitAbilities.gainEffectsAdjacentUnit(gameState, out, sourceTile);
            return;
        }
    }
}
