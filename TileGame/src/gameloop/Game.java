package gameloop;

import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.net.ssl.KeyManager;
import javax.sound.sampled.Clip;

import display.Display;
import gfx.ImageLoader;

public class Game implements Runnable {

	private Display display;
	public int width, height;
	public String title;
	
	private boolean running = false;
	private Thread thread;
	
	private BufferStrategy bs;
	private Graphics g;
	
	private int[][] board = new int[4][4];
	private final int W = 128, H = 128, SPACE=8;
	private final int X = 32, Y = 128;
	
	private boolean myWin = false;
	private boolean myLose = false;
	private int myScore = 0;
	
	private AudioHandler audio;
	
	public Game(String title, int width, int height){
		this.width = width;
		this.height = height;
		this.title = title;
		//keyManager = new KeyManager();
		
	}
	
	private BufferedImage bg;
	private BufferedImage[] testImage = new BufferedImage[13];
	private BufferedImage[] assets = new BufferedImage[2];
	
	private void init(){
		display = new Display(title, width, height);
		display.getFrame().addKeyListener(new KeyAdapter() {
		      @Override
		      public void keyPressed(KeyEvent e) {
		        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		          resetGame();
		        }
		        if (!canMove()) {
		        		System.out.println("!canMove");
		        		myLose = true;
		        }

		        if (!myWin && !myLose) {
		          switch (e.getKeyCode()) {
		            case KeyEvent.VK_LEFT:
		            	  audio.play("move", 0);
		              left();
		              break;
		            case KeyEvent.VK_RIGHT:
		            	audio.play("move", 0);
		              right();
		              break;
		            case KeyEvent.VK_DOWN:
		            	audio.play("move", 0);
		              down();
		              break;
		            case KeyEvent.VK_UP:
		            	audio.play("move", 0);
		              up();
		              break;
		          }
		        }

		        if (!myWin && !canMove()) {
		          myLose = true;
		        }
		        render();
		      }
		    });
		
		testImage[0] = ImageLoader.loadImage("/mp7/jobless.png");
		testImage[1] = ImageLoader.loadImage("/mp7/intern.png");
		testImage[2] = ImageLoader.loadImage("/mp7/employee.png");
		testImage[3] = ImageLoader.loadImage("/mp7/manager.png");
		testImage[4] = ImageLoader.loadImage("/mp7/assistance-director.png");
		testImage[5] = ImageLoader.loadImage("/mp7/director.png");
		testImage[6] = ImageLoader.loadImage("/mp7/senior-director.png");
		testImage[7] = ImageLoader.loadImage("/mp7/vice-president.png");
		testImage[8] = ImageLoader.loadImage("/mp7/president.png");
		testImage[9] = ImageLoader.loadImage("/mp7/CEO.png");
		testImage[10] = ImageLoader.loadImage("/mp7/vice-chairman.png");
		testImage[11] = ImageLoader.loadImage("/mp7/chairman.png");
		testImage[12] = ImageLoader.loadImage("/mp7/billionaire.png");
		
		assets[0] =  ImageLoader.loadImage("/congrats.png");
		assets[1] =  ImageLoader.loadImage("/003-game-over.png");

		bg = ImageLoader.loadImage("/bg.png");
		audio = AudioHandler.getInstance();
		audio.load("peng.wav", "peng");
		audio.load("move.wav", "move");
		audio.adjustVolume("move", -10);
		audio.load("boo.wav", "boo");
		audio.load("applause.wav", "applause");
		audio.load("HappyDay.wav", "BG");
		audio.adjustVolume("BG", -5);
		audio.play("BG", Clip.LOOP_CONTINUOUSLY);
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				board[i][j]=-1; // -1 is empty
			}
		}
		addTile();
	    addTile();
	}
	
	private void tick(){
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				if (!myWin && board[i][j] == 12) { // -1 is empty
					myWin = true;
					running = false;
					System.out.println("Congratulations! You are a billionaire!!");
					break;
				}
			}
		}
		if (!canMove()) {
    			//System.out.println("!canMove");
    			myLose = true;
		}
		if(myLose) { //  Lose
			running = false;
			System.out.println("You are not a billionaire in this life");
		}
	}
	
	private void render(){
		bs = display.getCanvas().getBufferStrategy();
		if(bs == null){
			display.getCanvas().createBufferStrategy(3);
			return;
		}
		g = bs.getDrawGraphics();
		//Clear Screen
		g.clearRect(0, 0, width, height);
		g.drawImage(bg, 0, 0, null);
		//Draw Here
		for(int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				if(board[i][j]>=0) {
					g.drawImage(testImage[board[i][j]],X+j*(W+SPACE), Y+i*(H+SPACE), null);
				}	
			}
			
		}
		if(myWin) {
			//System.out.println("lose and should draw");
			audio.play("applause", 0);
			g.drawImage(assets[0], 75, 200, null);
			
		} else if(myLose) { //  Lose
			//System.out.println("lose and should draw");
			audio.play("boo", 0);
			g.drawImage(assets[1], 175, 200, null);
		}
		//End Drawing!
		bs.show();
		g.dispose();
	}
	
	public void run(){
		init();
//		loseGame();
		winnerGame();
		int fps = 60;
		double timePerTick = 1000000000 / fps;
		double delta = 0;
		long now;
		long lastTime = System.nanoTime();
		long timer = 0;
		int ticks = 0;
		
		while(running){
			now = System.nanoTime();
			delta += (now - lastTime) / timePerTick;
			timer += now - lastTime;
			lastTime = now;
			
			if(delta >= 1){
				tick();
				render();
				ticks++;
				delta--;
			}
			
			if(timer >= 1000000000){
				System.out.println("Ticks and Frames: " + ticks);
				ticks = 0;
				timer = 0;
			}
		}
		
		stop();
		
	}
	
	public synchronized void start(){
		if(running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void stop(){
		if(!running)
			return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void resetGame() {
	    myScore = 0;
	    myWin = false;
	    myLose = false;
	    board = new int[4][4];
	    for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				board[i][j]=-1; // -1 is empty
			}
		}
	    addTile();
	    addTile();
	  }
	
	private void addTile() {
		int randTile = Math.random() < 0.9 ? 0 : 1;
		while(true) {
			int randx = (int)(Math.random()*4);
			int randy = (int)(Math.random()*4);
			if (board[randx][randy] < 0) { //empty
				board[randx][randy] = randTile;
				return;
			}
			if(isFull()) return;
		}
	}
	

	  private boolean isFull() {
		  for (int x = 0; x < 4; x++) {
		      for (int y = 0; y < 4; y++) {
		    	  	if (board[x][y] < 0) return false;
		      }
		  }
	    return true;
	  }

	  boolean canMove() {
	    if (!isFull()) {
	      return true;
	    }
	    for (int x = 0; x < 4; x++) {
	      for (int y = 0; y < 4; y++) {
	        if ((x < 3 && board[x][y] == board[x + 1][y])
	          || ((y < 3) && board[x][y] == board[x][y+1])) {
	          return true;
	        }
	      }
	    }
	    return false;
	  }
	  
	  public void left() {
		    System.out.println("left, myscore=" + myScore);
		    for(int i=1; i<4; i++) {
		    		for(int j=i; j>0; j--) {
		    			mergeLine(j, j-1, true);
		    		}
		    }
		    addTile();
	  }
	  
	  public void right() {
		    System.out.println("right, myscore=" + myScore);
		    for(int i=2; i>=0; i--) {
	    			for(int j=i; j<3; j++) {
	    				mergeLine(j, j+1, true);
	    			}
		    }
		    addTile();
	  }
	  
	  public void up() {
		    System.out.println("up, myscore=" + myScore);
		    for(int i=1; i<4; i++) {
		    		for(int j=i; j>0; j--) {
		    			mergeLine(j, j-1, false);
		    		}
		    }
		    addTile();
	  }
	  
	  public void down() {
		    System.out.println("down, myscore=" + myScore);
		    for(int i=2; i>=0; i--) {
	    			for(int j=i; j<3; j++) {
	    				mergeLine(j, j+1, false);
	    			}
		    }
		    addTile();
	  }
	  
	  private void mergeLine(int from, int to, boolean vertical) {
		  	boolean sound = false;
		    if(vertical) {
		    		for(int i=0; i<4; i++) {
		    			if (board[i][from]>=0 && board[i][from] == board[i][to]) {
		    				myScore += (board[i][to]+1)*2;
		    				board[i][from] = -1;
		    				board[i][to] += 1;
		    				sound = true;
		    			} else if (board[i][to]<0) {
		    				board[i][to] = board[i][from];
		    				board[i][from] = -1;
		    			}
		    		}
		    } else { // horizontal
			    	for(int i=0; i<4; i++) {
		    			if (board[from][i]>=0 && board[from][i] == board[to][i]) {
		    				myScore += (board[to][i]+1)*2;
		    				board[from][i] = -1;
		    				board[to][i] += 1;
		    				sound = true;
		    			} else if (board[to][i]<0) {
		    				board[to][i] = board[from][i];
		    				board[from][i] = -1;
		    			}
		    		}
		    }
		    if(sound) {
		    		audio.play("peng", 0);
		    }
	  }
	  
	  public void loseGame() {
		    myScore = 0;
		    myWin = false;
		    myLose = false;
		    board = new int[4][4];
		    for(int i=0; i<4; i++) {
				for(int j=0; j<4; j++) {
					board[i][j]=(i+j)%11; // -1 is empty
				}
			}
		    board[3][0] = -1;
		  }
	  
	  public void winnerGame() {
		    myScore = 0;
		    myWin = false;
		    myLose = false;
		    board = new int[4][4];
		    for(int i=0; i<4; i++) {
				for(int j=0; j<4; j++) {
					board[i][j]=-1; // -1 is empty
				}
			}
		    board[3][0]=11;
		    board[3][1]=11;
		  }
}











