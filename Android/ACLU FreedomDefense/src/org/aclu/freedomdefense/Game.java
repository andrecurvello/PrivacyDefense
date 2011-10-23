package org.aclu.freedomdefense;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Game implements ApplicationListener {
	public static int screenWidth = 480;
	public static int screenHeight = 320;

	
	public static final float TIME_BETWEEN_WAVES = 3;
	
	public static final int INITIAL_CREEP_SPEED = 32;

	public static int maxProjectiles = 1000;


	private SpriteBatch batch;
	private Texture spriteSheet;
	private Texture mapData;
	private BitmapFont mFont;
	private int[][] tiles; // Our base map (paths and whatnot)
	public char[][] movementDirs; // Our pathfinding, 'N' 'E' 'W' or 'S' (and
									// can make different for flyers, woah!)
	public int money;
	public int life;
	public ArrayList<Creep> creeps;
	public Projectile projectiles[];
	public ArrayList<Tower> towers;
	public ArrayList<TowerType> free_towers; 
	public int startingX, startingY;

	public int endingX;
	public int endingY;
	
	public int current_creep_speed = INITIAL_CREEP_SPEED;
	
	public boolean isPaused = false;

	public String debugtext = "";


	public float wave_wait_timer = 0;
	

	public TowerType cursorState;
	public int cursorLocX = 0;
	public int cursorLocY = 0;


	// Circle sprites for tower ranges
	HashMap<Float, Sprite> rangeSprites = new HashMap<Float, Sprite>();
	public final int maxmoney = 9999;
	public final int uiPanelWidth = 60;

	public static Game instance;
	
	public Rectangle collisionRectA;
	public Rectangle collisionRectB;
	
	// I'm sorry this is getting cruddy, so sleepy
	TextureRegion blackBox;
	TextureRegion tower_region;

	@Override
	public void create() {
		instance = this;
		
		// Our rects for collision detection
		collisionRectA = new Rectangle( 0, 0, 8, 8 );
		collisionRectB = new Rectangle( 0, 0, 8, 8 );
		
		Gdx.input.setInputProcessor(new GameInputProcessor());

		batch = new SpriteBatch();
		spriteSheet = new Texture(Gdx.files.internal("sprite_sheet.png"));
		Pixmap mapData = new Pixmap(Gdx.files.internal("map.png"));

		creeps = new ArrayList<Creep>();
		projectiles = new Projectile[maxProjectiles];
		
		for( int i = 0; i < 1000; ++i )
		{
			projectiles[i] = new Projectile( new Vector2( 0.0f, 0.0f ), new Vector2( 0.0f, 0.0f ), 0.0f );
			projectiles[i].active = false;
		}
		
		towers = new ArrayList<Tower>();

		towers.add(new Tower(TowerType.JUDGE, 6, 6));
		towers.add(new Tower(TowerType.LAWSUIT, 12, 4));
		towers.add(new Tower(TowerType.TEACHER, 19, 8));
		towers.add(new Tower(TowerType.JUDGE, 8, 8));
		towers.add(new Tower(TowerType.LAWSUIT, 4, 12));
		towers.add(new Tower(TowerType.TEACHER, 8, 19));
		
		free_towers = new ArrayList<TowerType>();
		free_towers.add(TowerType.JUDGE);
		free_towers.add(TowerType.FIREWALL);
		free_towers.add(TowerType.TEACHER);
		free_towers.add(TowerType.LAWSUIT);
		
		cursorState = null;

		tiles = new int[30][20];
		movementDirs = new char[30][20];

		mFont = new BitmapFont(Gdx.files.internal("ostrich_sans_mellow.fnt"), Gdx.files.internal("ostrich_sans_mellow.png"), false);
		mFont.setFixedWidthGlyphs("LifeMoney0123456789");

		money = 100;
		life = 49;

		// Feel free to change this, it is confusing!
		// Movement data is in the GREEN channel of the map:
		HashMap<Integer, Character> cToMoveDir = new HashMap<Integer, Character>();
		cToMoveDir.put(132, 'S');
		cToMoveDir.put(164, 'E');
		cToMoveDir.put(196, 'N');
		cToMoveDir.put(228, 'W');

		// Tile data is in the RED channel of the map:
		HashMap<Integer, Integer> cToTileNumber = new HashMap<Integer, Integer>();
		cToTileNumber.put(0, 4);
		cToTileNumber.put(32, 5);
		cToTileNumber.put(64, 8);
		cToTileNumber.put(96, 6);
		cToTileNumber.put(128, 7);
		cToTileNumber.put(160, 9);
		cToTileNumber.put(192, 10);

		for (int x = 0; x < 30; ++x) {
			for (int y = 0; y < 20; ++y) {
				int col = mapData.getPixel(x, y);

				int r = (col & 0xFF000000) >>> 24;
				int g = (col & 0x00FF0000) >>> 16;
				int b = (col & 0x0000FF00) >>> 8; // Currently have 100 for 1
													// below enemy spawn (so
													// they spawn offscreen),
													// 255 player base

				if (cToTileNumber.containsKey(r))
					tiles[x][19 - y] = cToTileNumber.get(r); // Flip the Y so
																// what we're
																// photoshopping
																// matches the
																// game
				else
					tiles[x][19 - y] = 4;

				if (cToMoveDir.containsKey(g))
					movementDirs[x][19 - y] = cToMoveDir.get(g);
				else
					movementDirs[x][19 - y] = 'S';

				if (b == 100) {
					startingX = x;
					startingY = 19 - y;
				} else if (b == 255) {
					endingX = x;
					endingY = 19 - y;
				}
			}
		}


		for( int i = 1; i < 50; ++i )
			creeps.add( new Creep( 100, current_creep_speed, 20, startingX, startingY + i, 0, 0, CreepType.PETTY ) );

		blackBox = new TextureRegion(spriteSheet, 0, 2 * 16, 16, 16);
		
		tower_region = new TextureRegion( spriteSheet, 0, 0, 16, 16 );
		
		mapData.dispose();
	}


	public void update() {
		float dt =2* Gdx.graphics.getDeltaTime();

		if (!isPaused && life > 0) {
			
			for (Projectile projectile : projectiles) 
			{
				if( projectile.active && projectile.my_coords.x > 0 && projectile.my_coords.y > 0 && projectile.my_coords.x * 16 < screenWidth && projectile.my_coords.y * 16 < screenHeight )
				{
					collisionRectA.x =  ( projectile.my_coords.x * 16 ) + 8;
					collisionRectA.y = ( projectile.my_coords.y * 16 ) + 8;
					
					for( int i = 0; i < creeps.size(); ++i )
					{
						if( creeps.get(i).active )
						{
							collisionRectB.x = ( creeps.get(i).x * 16 + creeps.get(i).xOffset ) + 8;
							collisionRectB.y = ( creeps.get(i).y * 16 + creeps.get(i).yOffset ) + 8;
							
							if( collisionRectA.overlaps( collisionRectB ) )
							{
								creeps.get(i).Health -= projectile.damage;
								projectile.active = false;
								break;
							}
						}
					}
				}
				
				if( projectile.active )
				{
					projectile.update(dt);
				}
			}
	
			int active_creeps = 0;
			
			// Handle projectile collision, creep update, creep death
			for( int i = 0; i < creeps.size(); ++i )
			{
				if( creeps.get(i).active )
				{
					if( creeps.get(i).Health > 0 )
					{
						creeps.get(i).update( dt );
						active_creeps++;
					}
					else
					{
						creeps.get(i).die();
					}
				}
			}

			for (Tower tower : towers) {
				tower.update(dt);
			}
		
			if (active_creeps <= 0 && wave_wait_timer < TIME_BETWEEN_WAVES) {
			
				wave_wait_timer += dt;
				System.out.println(wave_wait_timer);
				
			} else if (active_creeps <= 0 && wave_wait_timer >= TIME_BETWEEN_WAVES) 
			{
				current_creep_speed += 10;
				restart(current_creep_speed);
				wave_wait_timer = 0;
			}
		}
	}
		


	@Override
	public void render() {
		// LibGDX you are a MY-STERY TO ME
		update();

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT); // clear the screen
		batch.begin();

		// Draw the terrain!
		for (int x = 0; x < 30; ++x) {
			for (int y = 0; y < 20; ++y) {
				batch.draw(spriteSheet, x * 16, y * 16, tiles[x][y] * 16, 0, 16, 16);

				// Temporary, copy pasta
				/*
				 * switch( movementDirs[x][y] ) { case 'N': batch.draw(
				 * spriteSheet, x*16, y*16, 11*16, 0, 16, 16 ); break; case 'E':
				 * batch.draw( spriteSheet, x*16, y*16, 12*16, 0, 16, 16 );
				 * break; case 'W': batch.draw( spriteSheet, x*16, y*16, 13*16,
				 * 0, 16, 16 ); break; case 'S': batch.draw( spriteSheet, x*16,
				 * y*16, 14*16, 0, 16, 16 ); break; }
				 */
			}
		}

		// Draw the towers
		for( int i = 0; i < towers.size(); ++i )
		{
			drawSprite(towers.get( i ).getIconNum(), towers.get(i).m_x, towers.get(i).m_y);
		}

		// Draw the creeps!
		for( int i = 0; i < creeps.size(); ++i)
		{
			if( creeps.get(i).active )
				batch.draw(spriteSheet, creeps.get(i).x * 16 + creeps.get(i).xOffset, creeps.get(i).y * 16 + creeps.get(i).yOffset, 0, 16, 16, 16);
		}

		// Draw the projectiles!
		for( int i = 0; i < maxProjectiles; ++i )
		{
			if( projectiles[i].active )
			{
				// TODO: Change which sprite the projectile uses based on something in the projectile
				batch.draw( spriteSheet, projectiles[i].my_coords.x*16, projectiles[i].my_coords.y*16, 0, 16*3, 16, 16 );
			}
		}

		// Draw the UI!

		// Background
		batch.draw(blackBox, 0, 0, uiPanelWidth , screenHeight);

		// Draw the free towers.
		for (int i = 1; i <= 4; i++) 
		{
			if (free_towers.get(i-1) != null) 
			{
				tower_region.setRegion( free_towers.get(i-1).getSpriteLocX(), free_towers.get(i-1).getSpriteLocY(), 16, 16 );
				batch.draw(tower_region, 40, screenHeight - 48 * i, 16, 16);
				
				String towerPrice = "$" + free_towers.get(i-1).getPrice();
				TextBounds priceBounds = mFont.getBounds(towerPrice);
				mFont.drawWrapped(batch, towerPrice, 3, screenHeight - 48*i + priceBounds.height, priceBounds.width);
			}
		}
				
		// Text
		String uiString = "+: " + life + '\n' + "$: " + money;
		TextBounds uiBounds = mFont.getMultiLineBounds(uiString);
		mFont.drawWrapped(batch, uiString, 3, uiBounds.height + 3, uiBounds.width);

		// DEBUG TEXT
		mFont.drawWrapped(batch, debugtext, 60, uiBounds.height + 3, 1000);		
		

		// Render Paused String if needed in bottom right corner.
		String center_string = null;
		if (isPaused) {
			center_string = "PAUSED";
		} else if (life <= 0) {
			center_string = "GAME OVER";
		}
		
		if (center_string != null) {
			TextBounds pausedBounds = mFont.getMultiLineBounds(center_string);
			Color oldColor = mFont.getColor();
			mFont.setColor(Color.RED);
			mFont.drawWrapped(batch, center_string,
					(screenWidth / 2) - (pausedBounds.width / 2),
					(screenHeight / 2) - (pausedBounds.height / 2), pausedBounds.width );
			mFont.setColor(oldColor);
		}
		// is the player dragging a tower?
		if (cursorState != null){
			batch.draw(spriteSheet, cursorLocX, uiBounds.height - cursorLocY, 0, 16, 16, 16);

		}
		
		batch.end();
	}

	public void restart(int creep_speed) {
		
		for (int i = 0; i < creeps.size(); i++) {
			creeps.get(i).active = true;
			creeps.get(i).Health = 100;
			creeps.get(i).Speed = creep_speed;
			creeps.get(i).x = startingX;
			creeps.get(i).y = startingY + i;
		}
		
		life = 100;
		
	}
	
	public void drawSprite(int iconNum, int x, int y) {
		batch.draw(spriteSheet, x * 16, y * 16, iconNum * 16, 0, 16, 16);
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {
		if (life > 0) {
			isPaused = !isPaused;
		}
	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {

	}

	public void drawCircle(float radius, Color color, Vector2 position) {
		if (rangeSprites.containsKey(radius)) {
			Sprite rangedSprite = rangeSprites.get(radius);

			rangedSprite.setPosition(position.x - rangedSprite.getWidth() / 2, position.y - rangedSprite.getHeight() / 2);

			rangedSprite.draw(batch);

			return;
		}

		// The Pixmap's width+height needs to be a power of 2, so
		// calculate the next power of 2 based on the tower Range
		// * 2 (tower range goes both ways)
		int powof2 = 1;
		int shotRange = (int) Math.ceil(radius * 2);
		while (powof2 < shotRange){
			powof2 <<= 1;
		}

		// Create a new pixmap with the appropriate size
		Pixmap p = new Pixmap(powof2, powof2, Pixmap.Format.RGBA8888);
		p.setColor(color);

		// Tell the pixmap to create a circle with the middle point
		// shotRange / 2 (x and y), and a radius of shotRange / 2
		p.fillCircle(powof2 / 2, powof2 / 2, (int) radius);

		// Make a texture out of the pixmap, and use that to create
		// a Sprite
		Sprite uiTowerRangeCircle = new Sprite(new Texture(p));

		// We can now dispose the pixmap to free up its resources
		p.dispose();

		rangeSprites.put(radius, uiTowerRangeCircle);

		// Set the coordinates and draw the sprite
		uiTowerRangeCircle.setPosition(position.x - powof2 / 2, position.y - powof2 / 2);
		uiTowerRangeCircle.draw(batch);
	}
}