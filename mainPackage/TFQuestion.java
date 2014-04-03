package mainPackage;

public class TFQuestion extends Question
{
	
	char answer;
	
	public TFQuestion(String query, String pictureFilename, char answer, int worth, int lifespan)
	{
		super(2, query, pictureFilename, worth, lifespan);
		this.answer = answer;
	}
	
	public String toString()
	{
		return super.toString()+"\n"+answer;
	}
	
}
