package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is a representation of a Unit on the game board.
 * A unit has a unique id (this is used by the front-end.
 * Each unit has a current UnitAnimationType, e.g. move,
 * or attack. The position is the physical position on the
 * board. UnitAnimationSet contains the underlying information
 * about the animation frames, while ImageCorrection has
 * information for centering the unit on the tile. 
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file
	
	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;
	int health;
	int attack;
	String name;
	private boolean provoked = false;	//keeps track of whether the unit has been provoked
	private Unit provokedBy = null;		// keeps track of which unit it has been provoked by
	private boolean flying = false;
	private boolean rush = false;
	private boolean hasMoved=false;
	private boolean hasAttacked=false;
	private boolean isNewSummon=true;
	private boolean isStun=false;
	private int stunDuration=0;
	
	public Unit() {}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(0,0,0,0);
		this.correction = correction;
		this.animations = animations;
	}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(currentTile.getXpos(),currentTile.getYpos(),currentTile.getTilex(),currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
	}
	
	
	
	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public UnitAnimationType getAnimation() {
		return animation;
	}
	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}

	public void setHealth(int health){
		this.health=health;
	}

	public int getHealth(){
		return health;
	}

	public void setAttack(int attack){
		this.attack=attack;
	}

	public int getAttack(){
		return attack;
	}

	public void setName(String name){
		this.name=name;
	}

	public String getName(){
		return this.name;
	}

	
	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(),tile.getYpos(),tile.getTilex(),tile.getTiley());
	}


	// logic to be able to set a unit as provoked - for use by Provoke Ability
	public boolean isProvoked(){			// returns whether a unit has been provoked
		return provoked;
	}
	public void setProvoked(boolean provoked){
		this.provoked=provoked;
	}

	public Unit getProvokedBy() {			//returns the Unit that provoked caused the provoke
		return provokedBy;
	}
	public void setProvokedBy(Unit provokedBy) {
		this.provokedBy = provokedBy;
	}

	// getter and setter for rush ability
	public boolean hasRush() {
		return rush;
	}
	public void setRush(boolean rush) {
		this.rush = rush;
	}
	// getter and setter for flying
	public boolean hasFlying() {
		return flying;
	}
	public void setFlying(boolean flying) {
		this.flying = flying;
	}
	public boolean hasMoved(){
		return hasMoved;
	}
	public void setMove(boolean move){
		this.hasMoved=move;
	}
	public boolean hasAttacked(){
		return hasAttacked;
	}
	public void setAttacked(boolean attack){
		this.hasAttacked=attack;
	}
	public boolean isNewSummon(){
		return isNewSummon;
	}
	public void setNewSummon(boolean summon){
		this.isNewSummon=summon;
	}
	public boolean isStunned(){
		return isStun;
	}
	public void setStun(boolean stun){
		this.isStun=stun;
	}
	public int getStunDuration(){
		return this.stunDuration;
	}
	public void setStunDuration(int duration){
		this.stunDuration=duration;
	}
}
