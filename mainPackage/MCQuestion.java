package mainPackage;

public class MCQuestion extends Question
{

	String a, b, c, d;
	char answer;
	
	public MCQuestion(String query, String pictureFilename, String a, String b, String c, String d, char answer, int worth, int lifespan)
	{
		super(1, query, pictureFilename, worth, lifespan);
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.answer = answer;
	}
	
	public String toString()
	{
		return super.toString()+"\n"+a+"\n"+b+"\n"+c+"\n"+d+"\n"+answer;
	}
	
}
