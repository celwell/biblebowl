package mainPackage;

public class Question {

	// 1 = multiple-choice, 2 = true or false, 3 = neither
	int type;
	String query = "";
	String pictureFilename;
	int worth;
	int lifespan;
	boolean dead;
		
	public Question(int type, String query, String pictureFilename, int worth, int lifespan)
	{
		this.type = type;
		// the length of each line of the query should not exceed 54 characters; insert extra spaces, where necessary, to cause a new line when painted
		if (query.length() < 55)
			this.query = query;
		else
		{
			this.query += query.substring(0,query.lastIndexOf(" ", 55));
			int spacer = 55-query.lastIndexOf(" ", 55);
			for (int i=0; i<spacer; i++)
				this.query += " ";
			if ((query.length() + spacer) > 110)
			{
				this.query += query.substring(query.lastIndexOf(" ", 54)+1,query.lastIndexOf(" ", 110));
				int spacer2 = 110-query.lastIndexOf(" ", 110)-spacer;
				for (int i=0; i<spacer2; i++)
					this.query += " ";
				this.query += query.substring(query.lastIndexOf(" ", 110), query.length());
			}
			else
			{
				this.query += query.substring(query.lastIndexOf(" ", 54)+1,query.length());
			}
		}
		this.pictureFilename = pictureFilename;
		this.worth = worth;
		this.lifespan = lifespan;
		dead = false;
	}
	
	public String toString()
	{
		return ""+type+"\n"+query+"\n"+worth;
	}
}
