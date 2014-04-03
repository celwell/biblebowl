//  BibleBowl 1.1
//  This program is under GOD-Public License which means it can be freely distributed with or without the knowledge of the program writer/designer, and it means that any glory produced from the program should be ascribed directly to GOD.
//  However, I would be happy to hear any comments, corrections, or questions. Email me at yahwehagape@gmail.com
//  To work correctly, this java file (BibleBowl.java) requires 5 other java classes within its package.  These are: Question.java, TFQuestion.java, NEQuestion.java, MCQuestion.java, and Explosion.java
//  Teams depress their buzzers to show that they want to answer a question.  I made these buzzers by dismantling a keyboard and wiring the 'z','c','v', and 'm' key contacts to standard electrical buttons which can be bought at RadioShack

package mainPackage;

import java.awt.*;
import java.applet.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class BibleBowl extends Applet implements Runnable
{
	// used at start of program, in designating buzzers with color teams
	String[] configList = {"Unused", "Red", "Blue", "Green", "Orange", "Yellow", "Black", "Purple", "White", "Gray"};
	// used at start of program, in designating buzzers with color teams
	int[] configMem = {0,0,0,0};
	
	// 4 team MAX (because of the depressors (keyDown))
	String[] colors;
	Color[] colorsToPaintWith;
	int[] throbColorChooser;
	int[] scores;
	
	int timer;
	long startTime;
	// keeps track of how long in hotseat
	int giveAnswerTimer;
	long giveAnswerStartTime;
	// how long to give answer once already in hotseat
	final int GIVEANSWER_LIFESPAN = 4;
	double throbTime=1;
	
	// hotseat is who needs to answer the question (i.e., who depressed first)
	int hotseat = -1;
	// this is used to know who has to choose the category (start with first team)
	int lastOneCorrect=0;
	
	boolean configureGame;
	// when 'true': Final Jeopardy is in session
	boolean finalJeopardy = false;
	// whether or not Final Jeopardy already happened
	boolean finalJeopardyHappened = false;
	String finalJeopardyCategory = "";
	String finalJeopardyQuestion = "";
	ArrayList<String> finalJeopardyChoices;
	boolean categorySplashPhase = true;
	boolean thinkingSongAllowedToStart = false;
	boolean thinkingSongNeverPlayed = true;
	
	String scoreInput = "";
	int scoreInputTeamChoice = 0;
	boolean showScoreInputNumPad = false;
	
	boolean questionActive;
	boolean nextQuestionHighlighted = false;
	boolean aHighlight = false;
	boolean bHighlight = false;
	boolean cHighlight = false;
	boolean dHighlight = false;
	boolean outOfTimeHighlight = false;
	
	String messageUpdate;
	
	Question currQuestion;
	
	ArrayList<Question> bank;
	String[] colTitles = new String[5];
	
	ArrayList<Explosion> explosions;
	Explosion wolverineExplosion = new Explosion();
	int wolveCounter = 3;
	
	Font headingFont, headingShadowFont, questionFont, tileFont, colTitleFont;
	
	Image paperImage = Toolkit.getDefaultToolkit().getImage("images/paper.jpg");
	Image scoreImage = Toolkit.getDefaultToolkit().getImage("images/scoreParchment.gif");
	Image scoreHighlightedImage = Toolkit.getDefaultToolkit().getImage("images/scoreParchmentHighlighted.gif");
	Image nextQuestionImage = Toolkit.getDefaultToolkit().getImage("images/nextQuestionParchment.gif");
	Image nextQuestionHighlightedImage = Toolkit.getDefaultToolkit().getImage("images/nextQuestionParchmentHighlighted.gif");
	Image tileTablet = Toolkit.getDefaultToolkit().getImage("images/stoneTablet.gif");
	Image tileTabletDead = Toolkit.getDefaultToolkit().getImage("images/stoneTabletDead.gif");
	Image wolverine = Toolkit.getDefaultToolkit().getImage("images/wolverineFront.gif");
	
	Image buffer;
	Graphics2D b;
	
	AudioClip buzzInSound, introMusic, setConfigSound, deadTileSound, radarSound, correctSound, incorrectSound, thinkingSong, celebrationMusic;
	boolean celebrationMusicPlaying = false;
	
	Thread t;
	
	public void init()
	{
		resize(1024,768);
		buffer = createImage(getWidth(),getHeight());
		b = (Graphics2D)buffer.getGraphics();
		b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//b.setStroke(new BasicStroke(3));
		
		if (JOptionPane.showConfirmDialog(this, "Do you want there to be a Final Jeopardy Question at the end of this round?") == 1)
			finalJeopardyHappened = true;
		
		headingFont = new Font("Dalek", Font.BOLD, 64);
		headingShadowFont = new Font("Dalek", Font.BOLD, 65);
		questionFont = new Font("Dominican", Font.PLAIN, 37);
		tileFont = new Font("Dalek", Font.PLAIN, 50);
		colTitleFont = new Font("Dominican", Font.PLAIN, 20);
		
		finalJeopardyChoices = new ArrayList<String>();
		
		bank = new ArrayList<Question>();
		loadQuestions();
		
		messageUpdate = "";
		questionActive = false;
		
		configureGame = true;
		
		explosions = new ArrayList<Explosion>();
		wolverineExplosion.reset((int)(Math.random()*1024), (int)(Math.random()*768));
		
		buzzInSound = getAudioClip(getDocumentBase(),"sounds/buzzInSound.wav");
		introMusic = getAudioClip(getDocumentBase(),"sounds/jeopardyRemake.wav");
		setConfigSound = getAudioClip(getDocumentBase(),"sounds/entry.wav");
		deadTileSound = getAudioClip(getDocumentBase(),"sounds/deadTileSound.wav");
		correctSound = getAudioClip(getDocumentBase(),"sounds/correctSound.wav");
		incorrectSound = getAudioClip(getDocumentBase(),"sounds/incorrectSound.wav");
		thinkingSong = getAudioClip(getDocumentBase(),"sounds/thinkingSong.wav");
		celebrationMusic = getAudioClip(getDocumentBase(),"sounds/celebrationMusic.wav");
		radarSound = getAudioClip(getDocumentBase(),"sounds/radarSound.wav");
		
		introMusic.play();
		
		t = new Thread(this);
		t.start();
	}
	
	public void run()
	{
		while (true)
		{
			if (questionActive)
			{
				// temp time is for purposes of playing the radarSound when timer changes to certain time left
				int tempTime = timer;
				timer = currQuestion.lifespan-(int)(System.currentTimeMillis()-startTime)/1000;
				if (hotseat == -1)
				{
					if (tempTime > 3 && timer <= 3)
						radarSound.play();
					else if (tempTime > 2 && timer <= 2)
						radarSound.play();
					else if (tempTime > 1 && timer <= 1)
						radarSound.play();
				}
				// throbTime is used to pace the throb effect that occurs when a team buzzes into the hotseat
				throbTime = ((double)System.currentTimeMillis()-giveAnswerStartTime)/930;
				if (hotseat == -1 && timer <= 0 && currQuestion.lifespan != 0)
				{
					questionActive = false;
					if (currQuestion.type == 1)
					{
						if (((MCQuestion)currQuestion).answer == 'A')
							messageUpdate = "The answer was \""+((MCQuestion)currQuestion).a+"\".";
						else if (((MCQuestion)currQuestion).answer == 'B')
							messageUpdate = "The answer was \""+((MCQuestion)currQuestion).b+"\".";
						else if (((MCQuestion)currQuestion).answer == 'C')
							messageUpdate = "The answer was \""+((MCQuestion)currQuestion).c+"\".";
						else if (((MCQuestion)currQuestion).answer == 'D')
							messageUpdate = "The answer was \""+((MCQuestion)currQuestion).d+"\".";
					}
					else if (currQuestion.type == 2)
					{
						if (((TFQuestion)currQuestion).answer == 'T')
							messageUpdate = "The answer was \"True\".";
						else
							messageUpdate = "The answer was \"False\".";
					}
					else
					{
						messageUpdate = "";
					}
				}
				if (hotseat != -1)
				{
					giveAnswerTimer = GIVEANSWER_LIFESPAN-(int)(System.currentTimeMillis()-giveAnswerStartTime)/1000;
				}
			}
			try {t.sleep(5);}catch (Exception e) {}
			repaint();
		}
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}
	
	public void paint(Graphics g)
	{
		b.drawImage(paperImage, 0, 0, this);
		
		b.setColor(Color.black);
		b.setFont(headingFont);
		b.drawString("Bible Bowl - Teen Camp '08", 40, 90);
		
		if (!configureGame)
		{
			for (int i=0; i<colors.length; i++)
			{
				b.drawImage(scoreImage, 65+i*230, 115, this);
				b.setColor(Color.black);
				b.setFont(headingShadowFont);
				b.drawString(""+scores[i], 138+i*230-((String.valueOf(scores[i]).length()-1)*18), 201);
				b.setColor(colorsToPaintWith[i]);
				b.setFont(headingFont);
				b.drawString(""+scores[i], 136+i*230-((String.valueOf(scores[i]).length()-1)*18), 199);
			}
			
			if (questionActive)
			{
				if (currQuestion.pictureFilename != "!")
				{
					Image questionPicture = Toolkit.getDefaultToolkit().getImage("picturesForQuestions/"+currQuestion.pictureFilename);
					int pictureWidth = questionPicture.getWidth(this);
					int pictureHeight = questionPicture.getHeight(this);
					float pictureRatio = (float)pictureWidth / pictureHeight;
					if (pictureWidth >= pictureHeight)
					{
						if (pictureWidth > 424)
						{
							pictureHeight = (int)(1/pictureRatio*424);
							pictureWidth = 424;
						}
					}
					else
					{
						if (pictureHeight > 424)
						{
							pictureWidth = (int)(pictureRatio*424);
							pictureHeight = 424;
						}
					}
					
					//b.setColor(new Color(0,0,0,80));
					//b.fillRect(0, 0, getWidth(), getHeight());
					b.drawImage(questionPicture, 540-(pictureWidth/2), 556-(pictureHeight/2), pictureWidth, pictureHeight, this);
				}
				if (hotseat != -1)
				{
					if (throbTime < 1)
					{
						switch (throbColorChooser[hotseat])
						{
						case 1:
							b.setColor(new Color(255,0,0,(int)(throbTime*255)));
							break;
						case 2:
							b.setColor(new Color(0,0,255,(int)(throbTime*255)));
							break;
						case 3:
							b.setColor(new Color(0,255,0,(int)(throbTime*255)));
							break;
						case 4:
							//orange
							b.setColor(new Color(230,180,0,(int)(throbTime*255)));
							break;
						case 5:
							//yellow
							b.setColor(new Color(255,255,0,(int)(throbTime*255)));
							break;
						case 6:
							//black
							b.setColor(new Color(0,0,0,(int)(throbTime*255)));
							break;
						case 7:
							//purple
							b.setColor(new Color(255,0,255,(int)(throbTime*255)));
							break;
						case 8:
							//white
							b.setColor(new Color(255,255,255,(int)(throbTime*255)));
							break;
						case 9:
							//lightgray
							b.setColor(new Color(180,180,180,(int)(throbTime*255)));
							break;
						default:
							b.setColor(new Color(0,0,0,(int)(throbTime*255)));
						}
						b.fillRect(0, 0, getWidth(), getHeight());
					}
					else
					{
						//b.setColor(new Color(0,0,0,30));
						if (throbTime<2)
						{
							switch (throbColorChooser[hotseat])
							{
							case 1:
								b.setColor(new Color(255,0,0,(int)((2-throbTime)*255)));
								break;
							case 2:
								b.setColor(new Color(0,0,255,(int)((2-throbTime)*255)));
								break;
							case 3:
								b.setColor(new Color(0,255,0,(int)((2-throbTime)*255)));
								break;
							case 4:
								//orange
								b.setColor(new Color(230,180,0,(int)((2-throbTime)*255)));
								break;
							case 5:
								//yellow
								b.setColor(new Color(255,255,0,(int)((2-throbTime)*255)));
								break;
							case 6:
								//black
								b.setColor(new Color(0,0,0,(int)((2-throbTime)*255)));
								break;
							case 7:
								//purple
								b.setColor(new Color(255,0,255,(int)((2-throbTime)*255)));
								break;
							case 8:
								//white
								b.setColor(new Color(255,255,255,(int)((2-throbTime)*255)));
								break;
							case 9:
								//lightgray
								b.setColor(new Color(180,180,180,(int)((2-throbTime)*255)));
								break;
							default:
								b.setColor(new Color(0,0,0,(int)((2-throbTime)*255)));
							}
							b.fillRect(0, 0, getWidth(), getHeight());
						}
					}
					
					b.drawImage(scoreHighlightedImage, 65+hotseat*230, 115, this);
					b.setColor(Color.black);
					b.setFont(headingShadowFont);
					b.drawString(""+scores[hotseat], 138+hotseat*230-((String.valueOf(scores[hotseat]).length()-1)*18), 202);
					b.setColor(colorsToPaintWith[hotseat]);
					b.drawString(""+scores[hotseat], 141+hotseat*230-((String.valueOf(scores[hotseat]).length()-1)*18), 203);
				}
				b.setColor(Color.black);
				b.setFont(questionFont);
				if (currQuestion.type == 1)
				{
					MCQuestion q = (MCQuestion)currQuestion;
					if (q.query.length() > 110)
					{
						String first = q.query.substring(0, 55);
						String second = q.query.substring(55, 110);
						String third = q.query.substring(110, q.query.length());
						
						b.drawString(first, 65, 300);
						b.drawString(second, 65, 340);
						b.drawString(third, 65, 380);
					}
					else if (q.query.length() > 54)
					{
						String first = q.query.substring(0, 55);
						String second = q.query.substring(55, q.query.length());
						
						b.drawString(first, 65, 300);
						b.drawString(second, 65, 340);
					}
					else
					{
						b.drawString(q.query, 65, 300);
					}
					b.drawString("A. "+q.a, 65, 440);
					b.drawString("B. "+q.b, 65, 510);
					b.drawString("C. "+q.c, 65, 580);
					b.drawString("D. "+q.d, 65, 650);
				}
				else if (currQuestion.type == 2)
				{
					TFQuestion q = (TFQuestion)currQuestion;
					if (q.query.length() > 110)
					{
						String first = q.query.substring(0, 55);
						String second = q.query.substring(55, 110);
						String third = q.query.substring(110, q.query.length());
						
						b.drawString(first, 65, 300);
						b.drawString(second, 65, 340);
						b.drawString(third, 65, 380);
					}
					else if (q.query.length() > 54)
					{
						String first = q.query.substring(0, 55);
						String second = q.query.substring(55, q.query.length());
						
						b.drawString(first, 65, 300);
						b.drawString(second, 65, 340);
					}
					else
					{
						b.drawString(q.query, 65, 300);
					}
					b.drawString("True", 65, 440);
					b.drawString("False", 65, 510);
				}
				else if (currQuestion.type == 3)
				{
					NEQuestion q = (NEQuestion)currQuestion;
					if (q.query.length() > 110)
					{
						String first = q.query.substring(0, 55);
						String second = q.query.substring(55, 110);
						String third = q.query.substring(110, q.query.length());
						
						b.drawString(first, 65, 300);
						b.drawString(second, 65, 340);
						b.drawString(third, 65, 380);
					}
					else if (q.query.length() > 54)
					{
						String first = q.query.substring(0, 55);
						String second = q.query.substring(55, q.query.length());
						
						b.drawString(first, 65, 300);
						b.drawString(second, 65, 340);
					}
					else
					{
						b.drawString(q.query, 65, 300);
					}
					b.drawString("Correct", 65, 440);
					b.drawString("Incorrect", 65, 510);
				}
				
				b.setColor(new Color(0,0,0,30));
				if (aHighlight)
					b.fillRect(0, 390, 1024, 70);
				else if (bHighlight)
					b.fillRect(0, 460, 1024, 70);
				else if (cHighlight)
					b.fillRect(0, 530, 1024, 70);
				else if (dHighlight)
					b.fillRect(0, 600, 1024, 70);
				else if (outOfTimeHighlight)
					b.fillRect(0, 670, 1024, 70);
				
				// show timer if hotseat has not been filled yet and the question is a timed question
				if (hotseat == -1 && currQuestion.lifespan != 0)
				{
					b.setColor(new Color(0,0,0,50));
					b.fillOval(780, 530, 200, 200);
					
					if (timer > 7)
						b.setColor(Color.green);
					else if (timer > 3)
						b.setColor(Color.yellow);
					else
						b.setColor(Color.red);
					
					b.fillArc(780, 530, 200, 200, 90+360-(int)((float)timer/currQuestion.lifespan*360), (int)((float)timer/currQuestion.lifespan*360));
					b.setColor(Color.black);
					b.drawOval(780, 530, 200, 200);
					b.setFont(headingFont);
					b.drawString(""+timer, 860-((String.valueOf(timer).length()-1)*18), 667);
				}
				if (hotseat != -1)
				{
					if (giveAnswerTimer > 0)
					{
						b.setColor(new Color(0,0,0,50));
						b.fillOval(780, 530, 200, 200);
						
						b.setColor(Color.blue);
						
						b.fillArc(780, 530, 200, 200, 90+360-(int)((float)giveAnswerTimer/GIVEANSWER_LIFESPAN*360), (int)((float)giveAnswerTimer/GIVEANSWER_LIFESPAN*360));
						b.setColor(Color.black);
						b.drawOval(780, 530, 200, 200);
						b.setFont(headingFont);
						b.drawString(""+giveAnswerTimer, 860-((String.valueOf(giveAnswerTimer).length()-1)*18), 667);
					}
					else
					{
						b.setColor(Color.black);
						b.setFont(questionFont);
						b.drawString("Out of Time?", 790, 720);
					}
				}
			}
			else
			{
				b.setColor(Color.black);
				b.setFont(questionFont);
				
				if (!finalJeopardy)
					b.drawString(messageUpdate, 65, 275);
				
				// not saying that you won't use it for other playing styles, but for linear play: use here: if (bank.size() > 0)
				boolean allDead=true;
				for (int k=0; k<bank.size(); k++)
				{
					if (!bank.get(k).dead)
						allDead=false;
				}
				if (!allDead)
				{
					b.setFont(colTitleFont);
					b.drawString(colTitles[0], 155-(4*colTitles[0].length()), 330);
					b.drawString(colTitles[1], 330-(4*colTitles[1].length()), 330);
					b.drawString(colTitles[2], 505-(4*colTitles[2].length()), 330);
					b.drawString(colTitles[3], 680-(4*colTitles[3].length()), 330);
					b.drawString(colTitles[4], 855-(4*colTitles[4].length()), 330);
					b.drawString("Choose the Category", lastOneCorrect*230+76, 111);
					
					b.setFont(tileFont);
					int qCounter=0;
					for (int r=0; r<4; r++)
					{
						for (int c=0; c<5; c++)
						{
							if (bank.get(qCounter).dead)
								b.drawImage(tileTabletDead, c*175+75, r*105+345, this);
							else
								b.drawImage(tileTablet, c*175+75, r*105+345, this);				
							b.drawString(""+bank.get(qCounter).worth, c*175+140-(10*((""+bank.get(qCounter).worth).length()-1)), r*105+415);
							qCounter++;
						}
					}
					// the following is for the playing style that is a linear cycle through the questions in the bank
					/*
					if (bank.size() == 1)
					{
						if (bank.get(0).type == 1)
							b.drawString("Multiple-Choice :: "+bank.get(0).worth+" points.", 300, 600);
						else if (bank.get(0).type == 2)
							b.drawString("True-or-False :: "+bank.get(0).worth+" points.", 315, 600);
						else
							b.drawString("Open-Ended :: "+bank.get(0).worth+" points.", 325, 600);
			
						if (nextQuestionHighlighted)
							b.drawImage(nextQuestionHighlightedImage, 375, 440, this);
						else
							b.drawImage(nextQuestionImage, 375, 440, this);
						b.drawString("Final Question", 406, 498);
					}
					else
					{
						if (bank.get(0).type == 1)
							b.drawString("Multiple-Choice :: "+bank.get(0).worth+" points.", 300, 600);
						else if (bank.get(0).type == 2)
							b.drawString("True-or-False :: "+bank.get(0).worth+" points.", 315, 600);
						else
							b.drawString("Open-Ended :: "+bank.get(0).worth+" points.", 325, 600);
			
						if (nextQuestionHighlighted)
							b.drawImage(nextQuestionHighlightedImage, 375, 440, this);
						else
							b.drawImage(nextQuestionImage, 375, 440, this);
						b.drawString("Next Question", 406, 498);
					}
					*/
				}
				else
				{	
					if (!finalJeopardyHappened)
					{
						finalJeopardy = true;
						b.setFont(tileFont);
						b.drawString("-- Final Jeopardy --", 278, 280);
						b.setFont(questionFont);
						if (categorySplashPhase)
							b.drawString("Category: "+finalJeopardyCategory, 65, 325);
						else
						{
							thinkingSongAllowedToStart = true;
							if (finalJeopardyQuestion.length() > 110)
							{
								String first = finalJeopardyQuestion.substring(0, 55);
								String second = finalJeopardyQuestion.substring(55, 110);
								String third = finalJeopardyQuestion.substring(110, finalJeopardyQuestion.length());
								
								b.drawString(first, 65, 325);
								b.drawString(second, 65, 365);
								b.drawString(third, 65, 405);
							}
							else if (finalJeopardyQuestion.length() > 54)
							{
								String first = finalJeopardyQuestion.substring(0, 55);
								String second = finalJeopardyQuestion.substring(55, finalJeopardyQuestion.length());
								
								b.drawString(first, 65, 325);
								b.drawString(second, 65, 365);
							}
							else
							{
								b.drawString(finalJeopardyQuestion, 65, 325);
							}
							for (int c=0; c<finalJeopardyChoices.size(); c++)
							{
								b.drawString(finalJeopardyChoices.get(c), 65, 450+c*50);
							}
						}
					}
					else
					{
						int indexOfMax = 0;
						for (int i=0; i<colors.length; i++)
						{
							if (scores[i] > scores[indexOfMax])
								indexOfMax = i;
						}
						boolean tie = false;
						for (int i=0; i<colors.length; i++)
						{
							if (i != indexOfMax && scores[indexOfMax] == scores[i])
								tie = true;
						}
						if (!tie)
						{
							if (Math.random() < .1)
							{
								explosions.add(new Explosion());
								explosions.get(explosions.size()-1).reset((int)(Math.random()*1024), (int)(Math.random()*768));
								if (explosions.size() > 20)
									explosions.remove(0);
							}
							
							b.setColor(colorsToPaintWith[indexOfMax]);
							
							if (wolveCounter > 250)
							{
								for (int i=0; i<wolverineExplosion.getParticleCount(); i++)
									b.drawImage(wolverine, (int)wolverineExplosion.getPositionsX(i), (int)wolverineExplosion.getPositionsY(i), (int)((wolveCounter-250)*.47), (wolveCounter-250), this);
								wolverineExplosion.explode();
								wolverineExplosion.explode();
							}
							wolveCounter++;
							if (wolveCounter > 500)
							{
								wolveCounter = 250;
								wolverineExplosion.reset((int)(Math.random()*1024), (int)(Math.random()*768));
							}
							//b.drawImage(wolverine, 700-((int)(wolveCounter*.47)), 500-wolveCounter, (int)(wolveCounter*.47), wolveCounter, this); 
							
							for (int e=0; e<explosions.size(); e++)
							{
								for (int i=0; i<explosions.get(e).getParticleCount(); i++)
									b.fillRect((int)explosions.get(e).getPositionsX(i), (int)explosions.get(e).getPositionsY(i), 3, 3);
								explosions.get(e).explode();
							}
							
							b.setColor(Color.black);
							b.setFont(new Font("Dominican", Font.BOLD, 80));
							// capitalize first letter and make rest of team color lowercase
							String winner = colors[indexOfMax].toLowerCase();
							winner = winner.substring(1);
							winner = colors[indexOfMax].toUpperCase().charAt(0) + winner;
							b.drawString("The "+winner+" team wins!", 100, 510);
							if (!celebrationMusicPlaying)
							{
								celebrationMusic.play();
								celebrationMusicPlaying = true;
							}
						}
						else
						{
							b.setColor(Color.black);
							b.setFont(new Font("Dominican", Font.BOLD, 80));
							b.drawString("A tie-breaker is needed!", 100, 510);
						}
					}
				}
			}
		}
		else
		{
			b.setFont(questionFont);
			b.drawString("Tap the buzzer to cycle through the colors.", 150, 230);
			b.drawString("Press 'Enter' to set the configuration.", 150, 420+50*configMem.length);
			for (int i=0; i<configMem.length; i++)
			{
				switch (configMem[i])
				{
				case 0:
					b.setColor(Color.black);
					break;
				case 1:
					b.setColor(Color.red);
					break;
				case 2:
					b.setColor(Color.blue);
					break;
				case 3:
					b.setColor(Color.green);
					break;
				case 4:
					b.setColor(Color.orange);
					break;
				case 5:
					b.setColor(Color.yellow);
					break;
				case 6:
					b.setColor(Color.black);
					break;
				case 7:
					b.setColor(Color.magenta);
					break;
				case 8:
					b.setColor(Color.white);
					break;
				case 9:
					b.setColor(Color.lightGray);
					break;
				default:
					b.setColor(Color.black);
				}
				b.drawString("Buzzer "+i+":  "+configList[configMem[i]], 250, 350+50*i);
			}
		}
		/*
		if (showScoreInputNumPad)
		{
			b.setColor(new Color(0,0,0,185));
			b.fillRect(0, 0, getWidth(), getHeight());
			b.setColor(colorsToPaintWith[scoreInputTeamChoice]);
			b.fillRoundRect(308,146,408,458,92,92);
			b.setColor(new Color(255,255,255));
			b.fillRoundRect(312,150,400,450,90,90);
			b.setColor(Color.black);
			b.setFont(questionFont);
			b.drawString("Manual Score Change",346,205);
			b.setColor(colorsToPaintWith[scoreInputTeamChoice]);
			b.drawString(colors[scoreInputTeamChoice]+": "+scoreInput,346,255);
			b.setColor(Color.black);
		}
		*/

		g.drawImage(buffer,0,0,this);
	}
	
	public boolean mouseDown(Event e, int x, int y)
	{
		if (finalJeopardy)
		{
			categorySplashPhase = false;
			if (thinkingSongAllowedToStart && thinkingSongNeverPlayed)
			{
				thinkingSong.play();
				thinkingSongNeverPlayed = false;
			}
		}
		
		//for testing purposes to shortcut
		if (e.clickCount > 3)
		{
			for (int i=0; i<bank.size(); i++)
			{
				bank.get(i).dead = true;
			}
		}
		
		
		
		if (e.clickCount > 1)
		{
			for (int i=0; i<colors.length; i++)
			{
				//65+i*230, 135
				if (x>65+i*230 && x<265+i*230 && y>135 && y<225)
				{
					adjustScore(i);
					//scoreInputTeamChoice = i;
					//showScoreInputNumPad = true;
				}
			}
		}
		
		if (!configureGame && !questionActive && !finalJeopardy)
		{
			int qCounter=0;
			//tileTablet @ c*175+75, r*105+345
			for (int r=0; r<4; r++)
			{
				for (int c=0; c<5; c++)
				{
					if (x>c*175+75 && x<c*175+75+160 && y>r*105+345 && y<r*105+345+81)
					{
						if (!bank.get(qCounter).dead)
						{
							currQuestion = bank.get(qCounter);
							currQuestion.dead = true;
							startTime = System.currentTimeMillis();
							questionActive = true;
						}
						else
						{
							deadTileSound.play();
						}
					}
					qCounter++;
				}
			}
		}
		else if (hotseat != -1)
		{
			if (giveAnswerTimer < 1)
			{
				if (y > 670 && y < 740)
				{
					Question q = (Question)currQuestion;
					scores[hotseat] -= q.worth;
					//capitalize first letter and make rest of team color lowercase
					String temp = colors[hotseat].toLowerCase();
					temp = temp.substring(1);
					temp = colors[hotseat].toUpperCase().charAt(0) + temp;
					messageUpdate = "Out of time. The "+temp+" team loses "+q.worth+" points.";
					questionActive = false;
					hotseat=-1;
					incorrectSound.play();
				}
			}
			int choice = 0;
			if (y > 390 && y < 460)
				choice = 1;
			else if (y > 460 && y < 530)
				choice = 2;
			if (currQuestion.type == 1)
			{
				if (y > 530 && y < 600)
					choice = 3;
				else if (y > 600 && y < 670)
					choice = 4;
			}
			if (choice != 0)
			{
				if (currQuestion.type == 1)
				{
					MCQuestion q = (MCQuestion)currQuestion;
					char a='z';
					switch (choice)
					{
					case 1:
						a = 'A';
						break;
					case 2:
						a = 'B';
						break;
					case 3:
						a = 'C';
						break;
					case 4:
						a = 'D';
						break;
					}
					if (a == q.answer)
					{
						scores[hotseat] += q.worth;
						//capitalize first letter and make rest of team color lowercase
						String temp = colors[hotseat].toLowerCase();
						temp = temp.substring(1);
						temp = colors[hotseat].toUpperCase().charAt(0) + temp;
						messageUpdate = "Correct! The "+temp+" team gains "+q.worth+" points!";
						// to keep track of who chooses the next category
						lastOneCorrect = hotseat;
						questionActive = false;
						hotseat=-1;
						correctSound.play();
					}
					else
					{
						scores[hotseat] -= q.worth;
						//capitalize first letter and make rest of team color lowercase
						String temp = colors[hotseat].toLowerCase();
						temp = temp.substring(1);
						temp = colors[hotseat].toUpperCase().charAt(0) + temp;
						messageUpdate = "Incorrect. The "+temp+" team loses "+q.worth+" points.";
						questionActive = false;
						hotseat=-1;
						incorrectSound.play();
					}
				}
				else if (currQuestion.type == 2)
				{
					TFQuestion q = (TFQuestion)currQuestion;
					char a='z';
					if (choice == 1)
						a = 'T';
					else
						a = 'F';
					if (a == q.answer)
					{
						scores[hotseat] += q.worth;
						//capitalize first letter and make rest of team color lowercase
						String temp = colors[hotseat].toLowerCase();
						temp = temp.substring(1);
						temp = colors[hotseat].toUpperCase().charAt(0) + temp;
						messageUpdate = "Correct! The "+temp+" team gains "+q.worth+" points!";
						// to keep track of who chooses the next category
						lastOneCorrect = hotseat;
						questionActive = false;
						hotseat=-1;
						correctSound.play();
					}
					else
					{
						scores[hotseat] -= q.worth;
						//capitalize first letter and make rest of team color lowercase
						String temp = colors[hotseat].toLowerCase();
						temp = temp.substring(1);
						temp = colors[hotseat].toUpperCase().charAt(0) + temp;
						messageUpdate = "Incorrect. The "+temp+" team loses "+q.worth+" points.";
						questionActive = false;
						hotseat=-1;
						incorrectSound.play();
					}
				}
				else if (currQuestion.type == 3)
				{
					NEQuestion q = (NEQuestion)currQuestion;
					if (choice == 1)
					{
						scores[hotseat] += q.worth;
						//capitalize first letter and make rest of team color lowercase
						String temp = colors[hotseat].toLowerCase();
						temp = temp.substring(1);
						temp = colors[hotseat].toUpperCase().charAt(0) + temp;
						messageUpdate = "Correct! The "+temp+" team gains "+q.worth+" points!";
						// to keep track of who chooses the next category
						lastOneCorrect = hotseat;
						questionActive = false;
						hotseat=-1;
						correctSound.play();
					}
					else
					{
						scores[hotseat] -= q.worth;
						//capitalize first letter and make rest of team color lowercase
						String temp = colors[hotseat].toLowerCase();
						temp = temp.substring(1);
						temp = colors[hotseat].toUpperCase().charAt(0) + temp;
						messageUpdate = "Incorrect. The "+temp+" team loses "+q.worth+" points.";
						questionActive = false;
						hotseat=-1;
						incorrectSound.play();
					}
				}
			}
		}
		
		return true;
	}
	
	public boolean mouseMove(Event e, int x, int y)
	{
		aHighlight = false;
		bHighlight = false;
		cHighlight = false;
		dHighlight = false;
		outOfTimeHighlight = false;
		
		if (questionActive)
		{
			if (hotseat != -1)
			{
				if (y > 390 && y < 460)
					aHighlight = true;
				else if (y > 460 && y < 530)
					bHighlight = true;
				if (currQuestion.type == 1)
				{
					if (y > 530 && y < 600)
						cHighlight = true;
					else if (y > 600 && y < 670)
						dHighlight = true;
				}
				if (giveAnswerTimer < 1)
				{
					if (y > 670 && y < 740)
						outOfTimeHighlight = true;
				}
			}
		}
		
		return true;
	}
	
	public boolean keyDown(Event e, int k)
	{
		if (!thinkingSongNeverPlayed && k == 102)
			finalJeopardyHappened = true;
		if (questionActive && hotseat == -1)
		{
			switch (k)
			{
			case 122:
				hotseat=0;
				giveAnswerStartTime = System.currentTimeMillis();
				buzzInSound.play();
				break;
			case 99:
				if (colors.length > 1)
				{
					hotseat=1;
					giveAnswerStartTime = System.currentTimeMillis();
					buzzInSound.play();
				}
				break;
			case 118:
				if (colors.length > 2)
				{
					hotseat=2;
					giveAnswerStartTime = System.currentTimeMillis();
					buzzInSound.play();
				}
				break;
			case 109:
				if (colors.length > 3)
				{
					hotseat=3;
					giveAnswerStartTime = System.currentTimeMillis();
					buzzInSound.play();
				}
				break;
			}
		}
		else if (configureGame)
		{
			switch (k)
			{
			case 122:
				configMem[0]++;
				if (configMem[0] > configList.length-1)
					configMem[0]=0;
				break;
			case 99:
				configMem[1]++;
				if (configMem[1] > configList.length-1)
					configMem[1]=0;
				break;
			case 118:
				configMem[2]++;
				if (configMem[2] > configList.length-1)
					configMem[2]=0;
				break;
			case 109:
				configMem[3]++;
				if (configMem[3] > configList.length-1)
					configMem[3]=0;
				break;
			case 10:
				setConfiguration();
				configureGame = false;
				break;
			}
		}
		
		return true;
	}
	
	public void setConfiguration()
	{
		int teamCount=0;
		for (int i=0; i<configMem.length; i++)
		{
			if (configMem[i] != 0)
				teamCount++;
		}
		
		colors = new String[teamCount];
		colorsToPaintWith = new Color[teamCount];
		throbColorChooser = new int[teamCount];
		scores = new int[teamCount];
		
		int tempCount=0;
		for (int i=0; i<configMem.length; i++)
		{
			if (configMem[i] != 0)
			{
				colors[tempCount] = configList[configMem[i]];
				switch (configMem[tempCount])
				{
				case 0:
					colorsToPaintWith[tempCount] = Color.black;
					throbColorChooser[tempCount] = 0;
					break;
				case 1:
					colorsToPaintWith[tempCount] = Color.red;
					throbColorChooser[tempCount] = 1;
					break;
				case 2:
					colorsToPaintWith[tempCount] = Color.blue;
					throbColorChooser[tempCount] = 2;
					break;
				case 3:
					colorsToPaintWith[tempCount] = Color.green;
					throbColorChooser[tempCount] = 3;
					break;
				case 4:
					colorsToPaintWith[tempCount] = Color.orange;
					throbColorChooser[tempCount] = 4;
					break;
				case 5:
					colorsToPaintWith[tempCount] = Color.yellow;
					throbColorChooser[tempCount] = 5;
					break;
				case 6:
					colorsToPaintWith[tempCount] = Color.black;
					throbColorChooser[tempCount] = 6;
					break;
				case 7:
					colorsToPaintWith[tempCount] = Color.magenta;
					throbColorChooser[tempCount] = 7;
					break;
				case 8:
					colorsToPaintWith[tempCount] = Color.white;
					throbColorChooser[tempCount] = 8;
					break;
				case 9:
					colorsToPaintWith[tempCount] = Color.lightGray;
					throbColorChooser[tempCount] = 9;
					break;
				default:
					colorsToPaintWith[tempCount] = Color.black;
					throbColorChooser[tempCount] = 10;
				}
				scores[tempCount] = 0;
				tempCount++;
			}
		}
		
		introMusic.stop();
		setConfigSound.play();
	}
	
	public void adjustScore(int t)
	{
		scoreInput = JOptionPane.showInputDialog(this, "Input score for "+colors[t]+" manually:", "Bible Bowl - Manual Score Change", JOptionPane.QUESTION_MESSAGE);
		if (scoreInput == null || scoreInput.equalsIgnoreCase(""))
			return;
		for (int i=0; i<scoreInput.length(); i++)
		{
			char temp = scoreInput.charAt(i);
			// locate commas in case they want to pull this style: 2,300 or something.
			if (temp != ',')
			{
				// make sure they only use numbers
				if (temp != '0' && temp != '1' && temp != '2' && temp != '3' && temp != '4' && temp != '5' && temp != '6' && temp != '7' && temp != '8' && temp != '9')
				{
					// see, though, if they are trying to make the score negative (let them do this)
					if (temp != '-' || i!=0)
					{
						JOptionPane.showMessageDialog(this, "Only enter numbers!\nScore not changed.", "Bible Bowl - Manual Score Change", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			else
			{
				scoreInput = scoreInput.substring(0, i) + scoreInput.substring(i+1, scoreInput.length());
			}
		}
		scores[t] = Integer.parseInt(scoreInput);
	}
	
	public void loadQuestions()
	{
		FileInputStream dataFile;
		try
		{
			dataFile = new FileInputStream("round_3.txt");
			int k;
			colTitles[0] = "";
			colTitles[1] = "";
			colTitles[2] = "";
			colTitles[3] = "";
			colTitles[4] = "";
			while ((k = dataFile.read()) != 13)
			{
				colTitles[0] += (char)k;
			}
			dataFile.read();
			while ((k = dataFile.read()) != 13)
			{
				colTitles[1] += (char)k;
			}
			dataFile.read();
			while ((k = dataFile.read()) != 13)
			{
				colTitles[2] += (char)k;
			}
			dataFile.read();
			while ((k = dataFile.read()) != 13)
			{
				colTitles[3] += (char)k;
			}
			dataFile.read();
			while ((k = dataFile.read()) != 13)
			{
				colTitles[4] += (char)k;
			}
			dataFile.read();
			while ((k = dataFile.read()) != -1)
			{
				System.out.println(dataFile.available());
				if (k == 77)
				{
					if ((k = dataFile.read()) == 67)
					{
						System.out.println(dataFile.available());
						// must be a multiple-choice question
						String query = "";
						String pictureFilename = "";
						String a = "";
						String b = "";
						String c = "";
						String d = "";
						char answer;
						int worth;
						int lifespan;
						
						dataFile.read();
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						while ((k = dataFile.read()) != 13)
						{
							query += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						while ((k = dataFile.read()) != 13)
						{
							pictureFilename += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						while ((k = dataFile.read()) != 13)
						{
							a += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						while ((k = dataFile.read()) != 13)
						{
							b += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						while ((k = dataFile.read()) != 13)
						{
							c += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						while ((k = dataFile.read()) != 13)
						{
							d += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						System.out.println(dataFile.available());
						answer = (char)dataFile.read();
						dataFile.read();
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						String temp = "";
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							temp += (char)k;
							System.out.println(dataFile.available());
						}
						worth = Integer.parseInt(temp);
						dataFile.read();
						System.out.println(dataFile.available());
						temp = "";
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							temp += (char)k;
							System.out.println(dataFile.available());
						}
						lifespan = Integer.parseInt(temp);
						
						bank.add(new MCQuestion(query, pictureFilename, a, b, c, d, answer, worth, lifespan));
						
						// prepare dataFile for next time around while loop (get past the 10)
						dataFile.read();
					}					
				}
				else if (k == 84)
				{
					if ((k = dataFile.read()) == 70)
					{
						System.out.println(dataFile.available());
						// must be a true-or-false question
						String query = "";
						String pictureFilename = "";
						char answer;
						int worth;
						int lifespan;
						dataFile.read();
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						while ((k = dataFile.read()) != 13)
						{
							query += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						System.out.println(dataFile.available());
						while ((k = dataFile.read()) != 13)
						{
							pictureFilename += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						System.out.println(dataFile.available());
						answer = (char)dataFile.read();
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						String temp = "";
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							temp += (char)k;
							System.out.println(dataFile.available());
						}
						worth = Integer.parseInt(temp);
						dataFile.read();
						System.out.println(dataFile.available());
						temp = "";
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							temp += (char)k;
							System.out.println(dataFile.available());
						}
						lifespan = Integer.parseInt(temp);
						
						bank.add(new TFQuestion(query, pictureFilename, answer, worth, lifespan));
						
						// prepare dataFile for next time around while loop (get past the 10)
						dataFile.read();
						System.out.println(dataFile.available());
					}
				}
				else if (k == 78)
				{
					if ((k = dataFile.read()) == 69)
					{
						System.out.println(dataFile.available());
						// must be a 'neither' question
						String query = "";
						String pictureFilename = "";
						int worth;
						int lifespan;
						dataFile.read();
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						while ((k = dataFile.read()) != 13)
						{
							query += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						System.out.println(dataFile.available());
						while ((k = dataFile.read()) != 13)
						{
							pictureFilename += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						System.out.println(dataFile.available());
						String temp = "";
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							temp += (char)k;
							System.out.println(dataFile.available());
						}
						worth = Integer.parseInt(temp);
						dataFile.read();
						System.out.println(dataFile.available());
						temp = "";
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							temp += (char)k;
							System.out.println(dataFile.available());
						}
						lifespan = Integer.parseInt(temp);

						bank.add(new NEQuestion(query, pictureFilename, worth, lifespan));
						
						// prepare dataFile for next time around while loop (get past the 10)
						dataFile.read();
						System.out.println(dataFile.available());
					}					
				}
				else if (k == 70)
				{
					if ((k = dataFile.read()) == 74)
					{
						// Final Jeopardy information loads here
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						dataFile.read();
						System.out.println(dataFile.available());
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							finalJeopardyCategory += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						System.out.println(dataFile.available());
						while ((k = dataFile.read()) != 13 && k != -1)
						{
							finalJeopardyQuestion += (char)k;
							System.out.println(dataFile.available());
						}
						dataFile.read();
						System.out.println(dataFile.available());
						while (dataFile.available() != 0)
						{
							String choiceTemp = "";
							while ((k = dataFile.read()) != 13 && k != -1)
							{
								choiceTemp += (char)k;
								System.out.println(dataFile.available());
							}
							finalJeopardyChoices.add(choiceTemp);
							dataFile.read();
						}
					}
				}
				else
				{
					System.out.println("question.txt is not in the correct format!");
				}	
			}
			dataFile.close();
		}
		catch (IOException e)
		{
			System.out.println("Unable to read from file.\n" +e);
		}
	}
	
	
}
