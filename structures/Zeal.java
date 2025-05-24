package structures;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.*;
import utils.BasicObjectBuilders;

/*
Used for Zeal ability for the Silvernightguard - gain +2 attack when owning avatar takes damage
 */

public class Zeal implements AbilityTrigger {

    @Override
    public void trigger(Unit source, Unit target, GameState gameState,ActorRef out) {
        if (source.getName().equals("Silverguard Knight")) {
            int tx = source.getPosition().getTilex();
            int ty = source.getPosition().getTiley();
            Tile sourceTile = BasicObjectBuilders.loadTile(tx, ty);
            UnitAbilities.gainAttack(gameState, out, sourceTile, 2);
        }
    }
}

