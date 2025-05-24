package structures;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.Tile;
import utils.BasicObjectBuilders;
import java.util.List;



/*
This will mark enemy units adjacent to the provoke unit so they
are forced to attack units with Provoke ability,
 */
public class Provoke implements AbilityTrigger {

    @Override
    public void trigger(Unit source, Unit target, GameState gameState, ActorRef out) {
        int tx = source.getPosition().getTilex();
        int ty = source.getPosition().getTiley();
        Tile sourceTile = BasicObjectBuilders.loadTile(tx, ty);

        List<Unit> enemyUnits = UnitAbilities.provokeEnemyList(gameState, out, sourceTile);
        // returns a list of enemy units within range that are affected by the provoke ability
        for (int i = 0; i < enemyUnits.size(); i++ ) {
            enemyUnits.get(i).setProvoked(true);
            // iterates through the enemies and sets the provoke flag to true
        }
    }
}

