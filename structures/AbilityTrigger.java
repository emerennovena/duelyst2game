package structures;

import structures.basic.Unit;
import akka.actor.ActorRef;

public interface AbilityTrigger {
    void trigger(Unit source, Unit target, GameState gameState,ActorRef out);
}