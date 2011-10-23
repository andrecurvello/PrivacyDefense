package org.aclu.freedomdefense;

import com.badlogic.gdx.math.Vector2;

public class Tower {
	int m_x;
	int m_y;
	private final TowerType m_type;
	public int m_speed;
	public float m_timeToFire;
	public float m_radius;
	private float m_velocity;

	public Tower(TowerType type, int x, int y) 
	{
		m_type = type;
		m_x = x;
		m_y = y;
		
		if (type.equals(TowerType.JUDGE))
		{
			m_speed = 3;
			m_velocity = 6;
			m_radius = 40;
		}
		else if (type.equals(TowerType.FIREWALL))
		{
			m_speed = 6;
			m_velocity = 6;
			m_radius = 25;
		}
		else if (type.equals(TowerType.TEACHER))
		{
			m_speed = 7;
			m_velocity = 6;
			m_radius = 30;
		}
		else 
		{
			m_speed = 8;
			m_velocity = 6;
			m_radius = 35;
		}
		
		m_timeToFire = m_speed;
	}

	public void move(int x, int y) 
	{
		m_x = x;
		m_y = y;
	}
	
	public int getIconNum() 
	{
		if (m_type.equals(TowerType.JUDGE))
			return 0;
		else if (m_type.equals(TowerType.FIREWALL))
			return 2;
		else if (m_type.equals(TowerType.TEACHER))
			return 1;
		else 
			return 3;
	}
	
	public void update(float deltaTime) 
	{
		m_timeToFire -= deltaTime;
		
		// We're ready and there are creeps!
		if( m_timeToFire < 0 && Game.instance.creeps.size() > 0 ) 
		{
			// Find the enemy closest to the goal
			float closestDist = 999999.0f;
			int closestIndex = 0;
			
			for( int i = 0; i < Game.instance.creeps.size(); ++i )
			{
				//float dist2 = (float) (Math.pow( Game.instance.endingX - Game.instance.creeps.get(i).x, 2) + Math.pow( Game.instance.endingY - Game.instance.creeps.get(i).y, 2));
				float dist2 = (float) (Math.pow( m_x - Game.instance.creeps.get(i).x, 2) + Math.pow( m_y - Game.instance.creeps.get(i).y, 2));
				
				
				if( dist2 < closestDist ) {
					closestDist = dist2;
					closestIndex = i;
				}
			}
			
			if (closestDist < m_radius) {
				
				//Figure out direction
				int xDirection =  Game.instance.creeps.get(closestIndex).x - m_x;
				int yDirection =  Game.instance.creeps.get(closestIndex).y - m_y;
				Vector2 directionVector = new Vector2(xDirection, yDirection);
				Vector2 scaledDirectionVector = directionVector.nor().mul(m_velocity);

				Projectile projectile = new Projectile(new Vector2(m_x,m_y), scaledDirectionVector);
				Game.instance.projectiles.add(projectile);
				
				// Reset the cooldown for next frame
				m_timeToFire = m_speed;
			}
		}
	}
}
