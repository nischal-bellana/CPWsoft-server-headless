package com.game_states;

import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import com.Connection.RoomConnection;
import com.GameObjects.Bomb;
import com.GameObjects.BombContext;
import com.GameObjects.BombFactory;
import com.GameObjects.BombWorld;
import com.GameObjects.BombWorldFactory;
import com.GameObjects.Clip;
import com.GameObjects.Ground;
import com.GameObjects.GroundWorld;
import com.GameObjects.Player;
import com.GameObjects.PlayerWorld;
import com.GameObjects.myContactListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.utils.Clipping;
import com.utils.ParsingUtils;
import com.utils.PointNode;
import com.utils.WorldUtils;
import com.utils.polygonGen;

public class GameState extends State{
	private World world;
	private float accumulator;
	private Random random;
	
	private RoomConnection roomconnection;
	private StringBuilder removedindicesbroadcast;

	private Array<Player> players;
	private HashMap<Player, PlayerWorld> playersmap;
	private Player player;
	private int launchcount = 0;
	
	private BombFactory bombfactory;
	private BombWorldFactory bombworldfactory;
	private Array<Bomb> bombs;
	private HashMap<Bomb, BombWorld> bombsmap;
	private Queue<Bomb> bombsaddqueue;
	private Queue<Integer> bombsremovequeue;
	
	private Queue<Clip> clipsqueue;
	private Clip currentclip;
	
	private Ground ground;
	private GroundWorld groundworld;
	
	private UDPClientBridgeSender udpbridgesender;
	private UDPClientBridgeReceiver udpbridgereceiver;
	private StringBuilder broadcast;
	private StringBuilder udpbroadcast;
	
	private float timer = 0;
	private float turntimer = 0;
	private boolean gameended = false;
	private boolean shouldexit = false;
	private boolean inputchanged = false;
	private final float matchtime = 300;
	private final float turntime = 20;
	
	public GameState(RoomConnection roomconnection){
		this.roomconnection = roomconnection;
		create();
	}
	
	@Override
	protected void create() {
		// TODO Auto-generated method stub
		Box2D.init();
		world = new World(new Vector2(0,-9.8f), true);
		world.setContactListener(new myContactListener());
		random = new Random();
		broadcast = new StringBuilder();
		udpbroadcast = new StringBuilder();
		
		initUDPServerBridge();
		
		createGameObjects();
	}

	@Override
	protected void render() {
		// TODO Auto-generated method stub
		float delta = Gdx.graphics.getDeltaTime();
		
	    doPhysicsStep(delta);
	    
	    gameUpdate(delta);
	    if(shouldexit) return;
	    
	    inputUpdate();
	    
	    updatePlayers(delta);
	    
	    updateBombs(delta);
	    
	    updateClips(delta);
	    
	    groundworld.createFixtures();
	    groundworld.destroyFixtures();
	    
	    sendBroadcast();
	    
	    sendUDPBroadcast();
	    
	}

	@Override
	protected void dispose() {
		// TODO Auto-generated method stub
		roomconnection.setGameEnded();
		world.dispose();
		
		udpbridgesender.closeSocket();
		System.out.println("Closed Sender");
		udpbridgereceiver.closeSocket();
		System.out.println("Closed Receiver");
	}

	@Override 
	protected void resize(int width, int height) {
		// TODO Auto-generated method stub
	}
	
	private void createGameObjects(){
		
		clipsqueue = new Queue<>();
		
		bombfactory = new BombFactory();
		bombworldfactory = new BombWorldFactory(clipsqueue);
		bombs = new Array<Bomb>();
		bombsmap = new HashMap<>();
		bombsaddqueue = new Queue<Bomb>();
		bombsremovequeue = new Queue<Integer>();
		
		createGround();
		createPlayers();
	}
	
	private void createGround() {
//		polygonGen.imageToFixtures("images//atlasimages//grnd.png", "fonts//grnddata.xml");
		ground = new Ground();
		
		BodyDef bdef = new BodyDef();
		bdef.position.set(-2, 3);
		bdef.fixedRotation = true;
		bdef.type = BodyType.StaticBody;
		Body body = world.createBody(bdef);
		groundworld = new GroundWorld(ground, body);
		body.setUserData(groundworld);
		polygonGen.createCustomBody(world, "grnddata.xml", body, WorldUtils.createFixDef(1, 0.5f, 0), 1/(32f*3f));
		
		for(Fixture f : body.getFixtureList()) {
			PolygonShape shape = (PolygonShape)f.getShape();
			float[] arr = new float[2*shape.getVertexCount()];
			Vector2 vec = new Vector2();
			
			for(int i = 0; i < shape.getVertexCount(); i++) {
				shape.getVertex(i, vec);
				
				arr[2*i] = vec.x;
				arr[2*i+1] = vec.y;
				
			}
			
			ground.addFixture(arr);
			groundworld.addToBroadcastCreate(arr);
			
		}
	}
	
	private void createPlayers() {
		players = new Array<>();
		playersmap = new HashMap<>();
		
		for(int i = 0; i < roomconnection.size(); i++) {
			Player aplayer = createPlayer(roomconnection.getName(i), 5 + 3*i, 15);
			players.add(aplayer);
		}
		player = players.get(0);
	}
	
	private Player createPlayer(String name, float x, float y) {
		Player player = new Player(name);
		
		BodyDef bdef = new BodyDef();
		bdef.position.set(x, y);
		bdef.fixedRotation = true;
		bdef.type = BodyType.DynamicBody;
		FixtureDef fdef = WorldUtils.createFixDef(1f, 0.3f, 0);
		PolygonShape shp = new PolygonShape();
		shp.setAsBox(0.1f, 0.2f);
		fdef.shape = shp;
		Body playerbody = world.createBody(bdef);
		PlayerWorld playerworld = new PlayerWorld(playerbody, player);
		playersmap.put(player, playerworld);
		
		playerbody.setUserData(playerworld);
		playerbody.createFixture(fdef).setUserData(playerworld);
		fdef = WorldUtils.createFixDef(0.1f, 0.7f, 0.2f);
		shp.setAsBox(0.1f, 0.01f, new Vector2(0, -0.21f), 0);
		fdef.shape = shp;
		fdef.isSensor = true;
		playerbody.createFixture(fdef).setUserData(playerworld);
		shp.dispose();
		
		return player;
	}
	
	private void updatePlayers(float delta) {
		for(Player aplayer : players) {
			PlayerWorld playerworld = playersmap.get(aplayer);
			Body body = playerworld.getBody();
			aplayer.centerSpriteToHere(body.getPosition().x, body.getPosition().y );
			
			if(aplayer.getHealth() <= 0) {
				if(aplayer.getRespawnTime() > 3) {
					aplayer.respawn();
					aplayer.addDamage(-100);
					aplayer.addScorePoints(-20);
					body.setTransform((5 + 3 * random.nextInt(5)), 15, 0);
					body.setLinearVelocity(0, 0.01f);
					aplayer.resetRespawnTime();
				}
				else {
					aplayer.passrespawntime(delta);
				}
				continue;
			}
			
			if(body.getPosition().y < 3) {
				body.applyForceToCenter(0, 1f, false);
				aplayer.addDamage(10);
			}
			
			if(aplayer.getPowerLevel() != -1) aplayer.updatePowerIndicator();
		}
	}
	
	private void sendUDPBroadcast() {
		for(int i = 0; i < players.size; i++) {
			Player aplayer = players.get(i);
			Sprite sprite = aplayer.getSprite();
			udpbroadcast.append(sprite.getX() + (sprite.getWidth()/2));
			udpbroadcast.append(',');
			udpbroadcast.append(sprite.getY() + (sprite.getHeight()/2));
			
			udpbroadcast.append('#');
			
			udpbroadcast.append(aplayer.getPowerLevel() != -1 ? "t" : "f");
			if(aplayer.getPowerLevel() != -1) {
				udpbroadcast.append(aplayer.getPowerLevel());
				udpbroadcast.append(',');
				udpbroadcast.append(aplayer.getPowerSprite().getRotation());
			}
			
			udpbroadcast.append('&');
		}
		udpbroadcast.deleteCharAt(udpbroadcast.length() - 1);
		
		udpbroadcast.append(':');
		
		for(int i = 0; i < bombs.size; i++) {
			Sprite sprite = bombs.get(i).getSprite();
			udpbroadcast.append(sprite.getX());
			udpbroadcast.append(',');
			udpbroadcast.append(sprite.getY());
			udpbroadcast.append(',');
			udpbroadcast.append(sprite.getRotation());
			
			if(i != bombs.size - 1) udpbroadcast.append('#');
		}
		
		udpbroadcast.append(':');
		
		udpbroadcast.append((int)(matchtime - timer));
	    
		udpbroadcast.append(',');
		
		udpbroadcast.append((int)(turntime - turntimer));
		
		if(udpbroadcast.length() > 0) {
			udpbridgesender.addMessage(udpbroadcast.toString());
			udpbroadcast = new StringBuilder();
		}
	}
	
	private void sendBroadcast() {
		
		if(removedindicesbroadcast != null) {;
			removedindicesbroadcast.insert(0, 'p');
			ParsingUtils.appendData(removedindicesbroadcast.toString(), broadcast);
			removedindicesbroadcast = null;
			
		}
		
		if(inputchanged) {
			inputchanged = false;
			ParsingUtils.appendData("i" + roomconnection.getInputIndex(), broadcast);
		}
		
		boolean atleastone = false;
		
		StringBuilder damagedata = null;
		
		for(int i = 0; i < players.size; i++) {
			Player aplayer = players.get(i);
			int damage = aplayer.pollDamage();
			if(damage != -1) {
				if(!atleastone) {
					damagedata = new StringBuilder('d');
					atleastone = true;
				}
				
				ParsingUtils.appendData(i + "&" + damage, damagedata);
				aplayer.damageBy(damage);
			}
		}
		if(atleastone) {
			ParsingUtils.appendData(damagedata.toString(), broadcast);
		}
		
		atleastone = false;
		
		StringBuilder scoredata = null;
		
		for(int i = 0; i < players.size; i++) {
			Player aplayer = players.get(i);
			int scorepoint = aplayer.pollScorePoint();
			if(scorepoint != -1) {
				if(!atleastone) {
					scoredata = new StringBuilder('s');
					atleastone = true;
				}
				
				ParsingUtils.appendData(i + "&" + scorepoint, scoredata);
				aplayer.scoreBy(scorepoint);
			}
		}
		if(atleastone) {
			ParsingUtils.appendData(scoredata.toString(), broadcast);
		}
		
		if(bombsaddqueue.size - bombsremovequeue.size != 0) {
			ParsingUtils.appendData("b" + (bombsaddqueue.size - bombsremovequeue.size), broadcast);
			bombsaddqueue.clear();
			bombsremovequeue.clear();
		}
		
		atleastone = false;
		
		StringBuilder grounddata = null;
		
		if(groundworld.shouldBroadcastCreate()) {
			atleastone = true;
			grounddata = new StringBuilder('g');
			groundworld.getBroadcastCreate(grounddata);
		}
		
		if(groundworld.shouldBroadcastDestroy()) {
			if(!atleastone) {
				atleastone = true;
				grounddata = new StringBuilder('g');
			}
			groundworld.getBroadcastDestroy(grounddata);
		}
		
		if(atleastone) {
			ParsingUtils.appendData(grounddata.toString(), broadcast);
		}
		
		if(broadcast.length() > 0) {
			roomconnection.addBroadcast(broadcast.toString());
			broadcast = new StringBuilder();
		}
		
	}
	
	private void gameUpdate(float delta) {
		
		timer += delta;
		
		turntimer+=delta;
		
		if(turntimer > turntime) {
			roomconnection.incrementInputIndex(1);
			inputChanged();
		}
		
		removePlayers();
		
		if(roomconnection.size() < 2 || timer > matchtime) {
			shouldexit = true;
			Gdx.app.exit();
			return;
		}
		
	}
	
	private void inputUpdate() {
		
		String inputs = udpbridgereceiver.pollReturnMessage();
		if(inputs.equals("")) return;
		
		PlayerWorld playerworld = playersmap.get(player);
		Body body = playerworld.getBody();
		
		int encoded = getBitEncodedInputs(inputs);
		
		if(encoded % 2 == 1) {
			if(playerworld.inContact())
			body.setLinearVelocity(0, 5);
		}
		
		if((encoded >> 1) % 2 == 1) {
			body.applyForceToCenter(-0.1f, 0, false);
		}
		
		if((encoded >> 2) % 2 == 1) {
			body.applyForceToCenter(0.1f, 0, false);
		}
		
		if(launchcount < 3 && (encoded >> 3) % 2 == 1){
			player.setPowerLevel(player.getPowerLevel() == -1? 0 : -1);
		}
		
		if(launchcount < 3 && player.getPowerLevel() != -1) {
			adjustAngle(getPowerAngleInputs(inputs));
			
			touchInput((encoded >> 4) % 2 == 1, playerworld);
		}
		
	}
	
	private void updateBombs(float delta) {
		for(BombWorld bombworld : bombsmap.values()) {
			bombworld.update(delta);
		}
		
		for(int i = 0; i < bombs.size; i++) {
			Bomb bomb = bombs.get(i);
			BombWorld bombworld = bombsmap.get(bomb);
			
			if(!bomb.isAlive()) {
				bombs.removeIndex(i);
				bombsmap.remove(bomb);
				bombsremovequeue.addLast(i);
				continue;
			}
			
			bomb.setCenter(bombworld.getBody().getPosition().x, bombworld.getBody().getPosition().y);
			bomb.setRotation((float) Math.toDegrees(bombworld.getBody().getAngle()));
		}
		
	}
	
	private void updateClips(float delta) {
		if(currentclip == null) {
			currentclip = clipsqueue.isEmpty() ? null : clipsqueue.removeFirst();
			return;
		}

		if(currentclip.updateClip(delta)) currentclip = null;
	}
	
	private void doPhysicsStep(float deltaTime) {
	    // fixed time step
	    // max frame time to avoid spiral of death (on slow devices)
	    float frameTime = Math.min(deltaTime, 0.25f);
	    accumulator += frameTime;
	    while (accumulator >= 1/60f) {
	        world.step(1/60f, 6, 2);
	        accumulator -= 1/60f;
	    }
	}
	
	private void adjustAngle(float angle) {
		player.getPowerSprite().setRotation(angle);
	}
	
	private void touchInput(boolean touched, PlayerWorld playerworld) {
		if(touched) {
			player.incrementPowerLevel();
			return;
		}
		
		if(player.getPowerLevel() > 0) {
			BombContext context = playersmap.get(player).getBombContext();
			BombWorld bombworld = bombworldfactory.LaunchBomb(context);
			Bomb bomb = bombfactory.generateBomb();
			
			bombworld.setBomb(bomb);
			bombs.add(bomb);
			bombsmap.put(bomb, bombworld);
			bombsaddqueue.addLast(bomb);
			
			launchcount++;
			player.setPowerLevel(launchcount<3 ? 0 : -1);
		}
	}
	
	private void removePlayers() {
		int removedindex = roomconnection.pollRemovedIndex();
		
		if(removedindex == -1) return;
		
		removedindicesbroadcast = new StringBuilder();
		
		while(removedindex != -1) {
			
			removedindicesbroadcast.append(removedindex);
			removedindicesbroadcast.append('&');
			
			Player aplayer = players.removeIndex(removedindex);
			PlayerWorld playerworld = playersmap.remove(aplayer);
			playerworld.destroy();
			
			removedindex = roomconnection.pollRemovedIndex();
		}
		
		removedindicesbroadcast.deleteCharAt(removedindicesbroadcast.length() - 1);
		
		if(roomconnection.getInputIndex() >= roomconnection.size()) {
			roomconnection.setInputIndex(0);
			inputChanged();
		}
	}
	
	private void inputChanged() {
		player.setPowerLevel(-1);
		player = players.get(roomconnection.getInputIndex());
		inputchanged = true;
		turntimer = 0;
		launchcount = 0;
	}
	
	private void initUDPServerBridge() {
		try {
			udpbridgesender = new UDPClientBridgeSender();
			udpbridgereceiver = new UDPClientBridgeReceiver(udpbridgesender);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Thread udpthread1 = new Thread(udpbridgesender);
		udpthread1.start();
		
		Thread udpthread2 = new Thread(udpbridgereceiver);
		udpthread2.start();
		
	}
	
	private int getBitEncodedInputs(String inputs) {
		int res = 0;
		
		for(int i = 0; i < inputs.length(); i++) {
			char c = inputs.charAt(i);
			
			switch(c) {
			case 'w':
				res += 1;
				break;
			case 'a':
				res += 1<<1;
				break;
			case 'd':
				res += 1<<2;
				break;
			case 'c':
				res += 1<<3;
				break;
			case 't':
				res += 1<<4;
				break;
			}
			
		}
		
		return res;
	}
	
	private float getPowerAngleInputs(String inputs) {
		int i = 0;
		
		while(i < inputs.length() && Character.isAlphabetic(inputs.charAt(i))) {
			i++;
		}
		
		if(i >= inputs.length()) return 0;
		
		return ParsingUtils.parseFloat(i, inputs.length(), inputs);
	}
	
}
