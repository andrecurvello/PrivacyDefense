package org.aclu.freedomdefense;

public class Creep {
	boolean active;
	CreepType m_type;
	float Health, Speed, Money;
	public int x, y;
	float xOffset, yOffset;
	char direction;

	public Creep( float Health, float Speed, float Money, int x, int y, float xOffset, float yOffset, CreepType creepType )
	{
		this.m_type = creepType;
		this.Health = Health;
		this.Speed = Speed;
		// This money is obsolete.
		this.Money = Money;
		this.x = x;
		this.y = y;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		direction = 'S';
		active = true;
	}

	public void move( float dt )
	{
		switch( direction )
		{
		case 'N':
			yOffset +=  dt * Speed;
			break;
		case 'E':
			xOffset +=  dt * Speed;
			break;
		case 'W':
			xOffset -= dt * Speed;
			break;
		case 'S':
			yOffset -= dt * Speed;
			break;
		}
		
		if( xOffset > 24 )
		{
			++x;
			xOffset = 0;
		}
		if( xOffset < -24 )
		{
			--x;
			xOffset = 0;
		}
		if( yOffset > 24 )
		{
			++y;
			yOffset = 0;
		}
		if( yOffset < -24 )
		{
			--y;
			yOffset = 0;
		}
		
		if( x == Game.instance.endingX && y == Game.instance.endingY )
		{
			Game.instance.life--;
			Health = 0;
		}
	};

	public void update( float dt ) 
	{
		getNextDestinationCoordinate();
		
		move( dt );
	};

	public void getNextDestinationCoordinate()
	{
		// Creeps always come from North (for now)
		if( x >= 0 && x < Game.screenWidth / Game.SQUARE_WIDTH && y >= 0 && y < Game.screenHeight / Game.SQUARE_WIDTH )
			direction = Game.instance.movementDirs[x][y];
		else
			direction = 'S';
	};
	
	public void die()
	{
		Game.instance.money += m_type.getWorth();
		active = false;
	}
}
