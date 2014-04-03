package mainPackage;

public class Explosion
{
	// 100 = default
	private static int particles = 100;

	private double[][] positions = new double[particles][2];
	private double[][] velocities = new double[particles][2];

	public Explosion()
	{
		for (int a=0; a<particles; a++)
		{
			positions[a][0] = 0;
			positions[a][1] = 0;
			velocities[a][0] = 0;
			velocities[a][1] = 0;
		}
	}
	
	public void reset(int x, int y)
	{
		for (int a=0; a<particles; a++)
		{
			positions[a][0] = x;
			positions[a][1] = y;
			velocities[a][0] = (Math.random()-Math.random()*2)*1;
			velocities[a][1] = (Math.random()-Math.random()*2)*1;
		}
	}
	
	// ACCESSORS

	public int getParticleCount()
	{
		return particles;
	}
	
	public double getPositionsX(int p)
	{
		return positions[p][0];
	}
	
	public double getPositionsY(int p)
	{
		return positions[p][1];
	}
	
	public double getVelocitiesX(int p)
	{
		return velocities[p][0];
	}
	
	public double getVelocitiesY(int p)
	{
		return velocities[p][1];
	}
	
	// MUTATORS

	public void setPositionsX(int p, double n)
	{
		 positions[p][0] = n;
	}
	
	public void setPositionsY(int p, double n)
	{
		 positions[p][1] = n;
	}
	
	public void setVelocitiesX(int p, double n)
	{
		 velocities[p][0] = n;
	}
	
	public void setVelocitiesY(int p, double n)
	{
		 velocities[p][1] = n;
	}
	
	public void explode()
	{
		for (int a=0; a<particles; a++)
		{
			velocities[a][1] += .007;
			positions[a][0] += velocities[a][0];
			positions[a][1] += velocities[a][1];
		}
	}
}



