/* --------------------------------------------------------------------------
 * Move Tracker - gesture recognition engine 
 * --------------------------------------------------------------------------
 * prog:  Loris Leiva
 * date:  10/08/2014 (m/d/y)
 * ver:   1.0
 * ----------------------------------------------------------------------------
 */

// === setup ==================================================

boolean useFullscreen = false;
int nbrOfMoves = 11; // TODO : back to 10
int nbrOfPerson = 2;


// === variables defined by XML ===============================

// gener
boolean autoPoseDetection = false;
boolean useMultiThreading = true;

// for all gestures
boolean NORMALIZE_SIZE = true;
int framesGestureMax = 25;

// default gesture settings
float defaultWeightX = 1.0;
float defaultWeightY = 1.0;
float defaultWeightZ = 1.0;
float defaultWeightLeftOrRight = 0.0;
boolean defaultNormRotation = true;
int defaultFramesGesture = framesGestureMax;


// === libraries ==============================================

// import fullscreen.*; 
// FullScreen fs; 

import SimpleOpenNI.*;
import oscP5.*;
import netP5.*;
import java.util.Map;
// import processing.opengl.*;


// === globals variables ======================================

SimpleOpenNI context;
int framesInputMax = 2*framesGestureMax;

int counter = 0;
int counterEvent = 0;

int person = 0;
boolean foundSkeleton = false;

// Relative Array of objects
Pose[][] grid;
Move[] moves;
Map<Integer, User> users;



int steps[];
float speed[];
float cost[][];
float costLast[][];
boolean empty[];
PGraphics pg;
int warning[];

Data data;
Server server;
GUI gui;





/* =====================================================================================
 setup
 ===================================================================================== */
void setup()
{
  // Instanciations
  users = new HashMap<Integer,User>();
  data = new Data();
  server = new Server();

  // Arrays instanciations
  steps = new int[nbrOfMoves];
  speed = new float[nbrOfMoves];
  cost = new float[nbrOfPerson][nbrOfMoves];
  costLast = new float[nbrOfPerson][nbrOfMoves];
  empty = new boolean[nbrOfMoves];
  warning = new int[nbrOfPerson];
  moves = new Move[nbrOfMoves];
  
  for (int i = 0; i < nbrOfMoves; i++)
    moves[i] = new Move();

  // parse XML setup file
  parseXML();

  if (useMultiThreading)
    context = new SimpleOpenNI(this, SimpleOpenNI.RUN_MODE_MULTI_THREADED); 
  else
    context = new SimpleOpenNI(this);
  if (context.isInit() == false)
  {
    println("Can't init SimpleOpenNI, maybe the camera is not connected!"); 
    exit();
    return;
  }

  grid = new Pose[framesInputMax][framesGestureMax];
  for (int i = 0; i < framesInputMax; i++)
    for (int j = 0; j < framesGestureMax; j++)
      grid[i][j] = new Pose();

  File f;
  for (int i = 0; i < nbrOfMoves; i++) {
    String str = Integer.toString(i); 
    f = new File(dataPath("pose" + str + ".data"));      
    if (!f.exists()) {
      println("File " + dataPath("pose" + str + ".data") + " does not exist");
      for (int p = 0; p < nbrOfPerson; p++)
        cost[p][i] = 10000.0;
    } else { 
      data.loadMove(i);
    }
  }

  // Warnings
  warning[0] = -1;
  warning[1] = -1;

  // enable depthMap generation & skeleton for particular joints
  context.enableDepth();
  context.enableUser();

  // Setup the GUI
  gui = new GUI();
}


void draw()
{
  gui.drawMainPage();
}

// draw the skeleton with the selected joints
void evaluateSkeleton(int userId)
{
  // capture and draw
  Pose pose = (new Pose()).capture(context, userId);
  if (NORMALIZE_SIZE) pose.normalizeSize();

  // add to the buffer
  users.get(userId).fillBuffer(pose);
}

// -----------------------------------------------------------------
// SimpleOpenNI events

void onNewUser(SimpleOpenNI kinect, int userId)
{
  User user = new User(userId);
  users.put(userId, user);
  println("onNewUser - userId: " + userId);
  user.hello();
  kinect.startTrackingSkeleton(userId);
}

void onLostUser(SimpleOpenNI kinect, int userId)
{
  users.remove(userId);
  println("onLostUser - userId: " + userId);
  //  if (!autoPoseDetection)
  //    context.stopTrackingSkeleton(userId);
}

void onCompletedGesture(SimpleOpenNI context, int gestureType, PVector pos)
{
  println("onCompletedGesture - gestureType: " + gestureType + ", pos: " + pos);

  int handId = context.startTrackingHand(pos);
  println("hand stracked: " + handId);
}

void onNewHand(SimpleOpenNI curContext, int handId, PVector pos) {
}
void onTrackedHand(SimpleOpenNI curContext, int handId, PVector pos) {
}
void onLostHand(SimpleOpenNI curContext, int handId) {
}

// -----------------------------------------------------------------
// Keyboard events

void keyPressed()
{  
  if ( (key >= '0') && (key <= '9') && (foundSkeleton) )
  {
    int keyIndex = key-'0';

    int[] userList = context.getUsers();
    if (userList.length < 1)
      return;

    println("POSE " + keyIndex + " SAVED");
    users.get(userList[0]).saveMyMove(moves[keyIndex]);

    String str = Integer.toString(keyIndex);
    pg.save(dataPath("pose" + str + ".png")); 
    data.saveMove(keyIndex);
    gui.foto[keyIndex] = loadImage(dataPath("pose" + str + ".png"));
    empty[keyIndex] = false;
    gui.update();
  }

  switch(key)
  {
  case 'c': 
    pg.save ("capture.png");
    break; 

  case '+':
    gui.nextCost();
    break;

  case '-':
    gui.prevCost();
    break;

  case 'd':
    gui.switchDisplay();
    break;
  }
}
