package structures;

import java.lang.management.PlatformLoggingMXBean;
import java.util.*;
import structures.basic.*;
import utils.OrderedCardLoader;

public class GameState {

	public boolean gameInitalised = false;
	public boolean something = false;
	public Tile startTile=null;
	public List<Unit> humanPlayerUnitList=new ArrayList<>();
	public List<Unit> aiPlayerUnitList=new ArrayList<>();
	public Unit humanPlayer=null;
	public Unit aiPlayer=null;
	public int isMoved=0;
	public Unit readyToMove=null;
	public int isAttacked=0;
	public Unit readyToAttack=null;
	public List<Tile> moveTileList=new ArrayList<>();
	public List<Tile> board=new ArrayList<>();
	public int handPosition=0;
	public int unitId=0;
	public Unit provokeUnit=null;
	public int isSpellTarget=0;
	public int isHumanAttacked=0; 
	public int keepOriginalHealth=0; 
	public List<Card> aiPlayerCardList=OrderedCardLoader.getPlayer2Cards(2); 

}
	

