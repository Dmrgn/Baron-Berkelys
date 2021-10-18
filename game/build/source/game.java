import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.File; 
import javax.swing.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class game extends PApplet {

// This is the main game. It uses a shader that draws shadows and has a camera that tracks the player.
//I have fallen victim to the classic trap of having your plan work too well
//my intial goal was to create much better graphics than you would see in other processing games
//however to acheive this goal, I spent much more time focusing on graphics over gameplay
//this mean most of the gameplay portion of my game was left for latter some still to be complete
//for now the game operates more as a graphics library than a game
//i will be submitting an improved version that acts more like a true 'game'

//baron berkely's best bounty hunting hierarchy for menacing magical creatures and contaminant

//use 'l' then enter "night" or "day" to see examples

//CONTROLS
//Portal:
//the portal is used to move entities like chickencorns
//you can hold left click while hovering over an entity to hide it
//you can hold right click to make the hiden entities reappear at the mouse position
//Push Blocks
//you can push 'push blocks' out of the way by walking into it in the direction you want to push it
//push blocks are grey with a black arrow indicator above them
//Movement:
// w/up arrow: player forward
// s/down arrow: player down
// d/right arrow: player right
// a/left arrow: player left
//Debugging:
// g: tile grid
// k: area grid

//STORY
//set to false to see the story mode
boolean ingame = false;
boolean story = false;

//PERFORMANCE
//if you are having issues with performance, you can set the following to a HIGHER value (recommended 2)
float quality = 2;

//RESOLUTION
//By default the screen is 1920 by 1080, if this is too big you can adjust size() below (maintain 1920,1080 aspect ratio)

//VARIABLE NAMING
//variable prefix 'i' stands for import
//variable prefix 'ex' stands for export
//variable prefix 'd' stands for data
//variable prefix 'j' stands for json
//variable prefix 'temp' stands for temporary (used for changing length of array etc.)

//if a variable has an underscore (it generally will have a long name) it is an important variable
//for example camera_position controls where EVERYTHING is drawn or draw_canvas_layer() draws EVERYTHING

//camera globals
PVector camera_position = new PVector(0,0);
float camera_distance = 2;

//player global
player drudge;
baron berkely;

//graphics globals
PGraphics canvas;
PGraphics litscreen;
PGraphics postprocessing;
PShader fragshader;
PImage perlin;//perlin noise
float sratio;//screen ratio width/height
float time = 200;
JSONArray lights = new JSONArray();

//layer and scene globals
String layer = "base";
String[] layers = {"base","decorations","areas"};
JSONObject dareas;
JSONObject dtiles;
JSONObject ddecorations;
JSONObject denemys;
JSONArray tilemanifest;
JSONArray areamanifest;
JSONArray decorationmanifest;
JSONArray enemymanifest;
PImage[] tileimages = new PImage[0]; //a list of previously loaded tile images
String[] dtileimages = new String[0]; //manifest for tileimages
PImage[] decorationimages = new PImage[0]; //a list of previously loaded decoration images
String[] ddecorationimages = new String[0]; //manifest for decorationimages
JSONArray[] allmanifest = new JSONArray[4];
String menutext = "";

tile[][] tiles;//tiles array
enemy[] enemies;//enemies
portal pportal;//player portal

public void setup() {
  
  

  sratio = ((float)width/(float)quality)/((float)height/(float)quality);//screen ratio with quality
  fragshader = loadShader("fragshader.glsl");
  canvas = createGraphics(width,height,P2D);
  postprocessing = createGraphics(width,height,P2D);
  postprocessing.smooth(8);
  litscreen = createGraphics(PApplet.parseInt(width/quality),PApplet.parseInt(height/quality),P2D);
  litscreen.smooth(8);

  perlin = loadImage("textures/perlin.png");

  File dir = new File(dataPath("scenes"));
  files = dir.listFiles();//load json levels list from scenes

  drudge = new player(128*3,128*3);
  enemies = new enemy[0];
  pportal = new portal(0,0);
  berkely = new baron(0,0);

  //load tile json
  load_json();

  //load menu ui
  load_menu_assets();

  tiles = new tile[30][30];
  for (int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      if (floor(random(0,2)) == 1) {
        tiles[i][j] = new tile(i, j, "grass");
      } else {
        tiles[i][j] = new tile(i, j, "plains");
      }
    }
  }
}

public void draw() {
  //show framerate in window bar
  surface.setTitle("FPS: " + frameRate);
  if (ingamemenu && !isdying) {//if in the menu while in a game
    drawingamemenu();
  } else if(story && !ingame && !isdying) {//if the story is being run
    play_story();//(levelcontroller.pde)
  }
  if(ingame && !ingamemenu) {//if the game is actually running

    //update player coords if not dying
    if (!isdying) drudge.update();//(player.pde)

    //draw
    draw_canvas_layer();//everything (graphicscontroller.pde)
    draw_litscreen_layer();//all things that interact with light (graphicscontroller.pde)
    draw_postprocessing_layer();//fog/special effects (graphicscontroller.pde)

    //update portal and enemies
    if (!isdying) pportal.update();//(player.pde)
    for (int i = 0 ; i < enemies.length; i++) {
      if (!isdying) enemies[i].update();
    }

    //handle lights and lighting
    handle_lights();//(graphicscontroller.pde)
    lights = new JSONArray();
    //
    pushMatrix();
    scale(camera_distance,camera_distance);
    background(0);
    blendMode(NORMAL);
    image(canvas, 0, 0, width, height);
    blendMode(MULTIPLY);
    image(litscreen, 0, 0,width,height);
    blendMode(ADD);
    image(postprocessing, 0, 0,width,height);
    blendMode(NORMAL);
    popMatrix();

    //hard coded win conditions and mechanics
    if (!isdying) do_hard_coded();//(levelcontroller.pde)

    //draw ui as top layer
    if (!isdying) draw_ui();//(uicontroller.pde)

    //if the player should be playing their death animation
    if (isdying) displaydeath();
  } else if (!story && !ingame){
    draw_menu();//(uicontroller.pde)
  }
}
class baron {
  PVector pos = new PVector(0,0);
  PImage[] textures = new PImage[2];
  int curframe = 0;
  baron(float x, float y) {
    pos = new PVector(x,y);
    textures[0] = loadImage("textures/characters/baron1.png");
    textures[1] = loadImage("textures/characters/baron2.png");
  }
  public void show() {
    canvas.noStroke();
    canvas.image(textures[curframe],pos.x,pos.y);
    if (frameCount % 60 == 0) {
      curframe++;
      curframe = curframe%2;
    }
  }
  public void changepos(float x, float y) {
    pos = new PVector(x, y);
  }
}
//moves the camera around player and keeps track of player input
boolean cutscene = false;
boolean hold = false; //go straight to cutscene point and hold

public void position_camera_canvas() {
  camera_position = new PVector(-drudge.pos.x+(width/2/camera_distance)-64,-drudge.pos.y+(height/2/camera_distance)-64);
  if(cutscene) {
    camera_position.sub(cutamount);
  }
  canvas.translate((int)camera_position.x,(int)camera_position.y);
}
public void position_camera_litscreen() {
  camera_position = new PVector(-drudge.pos.x+(width/2/camera_distance)-64,-drudge.pos.y+(height/2/camera_distance)-64);
  if(cutscene) {
    camera_position.sub(cutamount);
  }
  litscreen.translate(PApplet.parseInt(camera_position.x/quality),PApplet.parseInt(camera_position.y/quality));
}
public void position_camera_postprocessing() {
  camera_position = new PVector(-drudge.pos.x+(width/2/camera_distance)-64,-drudge.pos.y+(height/2/camera_distance)-64);
  if(cutscene) {
    camera_position.sub(cutamount);
  }
  postprocessing.translate((int)camera_position.x,(int)camera_position.y);
}

//handle player movement
int upPressed = 0;
int downPressed = 0;
int rightPressed = 0;
int leftPressed = 0;
boolean rmousedown = false;
boolean lmousedown = false;
boolean skip = false;

boolean show_areas = false;
public void keyPressed() {
  if (key == 'g') {
    grid = !grid;
  }
  if (key == 'k') {
    show_areas = !show_areas;
  }
  if (key == ' ') {
    if (ingame && ingamemenu) {
      ingamemenu = false;
    } else if (ingame && !ingamemenu) {
      ingamemenu = true;
    }
    if ((!ingame && !story) && (whichmenu.equals("options") || whichmenu.equals("credits"))) {//if in one of menus subcompoenets
      nextismenu = true;
      transition = true;
      transamount = 0;
    } else {//if not in game and not in main menu (in prelevel cutscene)
      skip = true;
    }
  }
  if (key == 'l') {
    String path = getString("What is the file called? (don't include .json)");
    loadscene("scenes/"+path+".json");
  }
  if (keyCode == UP || key == 'w') {
    upPressed = 1;
  }
  else if (keyCode == DOWN || key == 's') {
    downPressed = 1;
  }
  else if (keyCode == LEFT || key == 'a') {
    leftPressed = 1;
  }
  else if (keyCode == RIGHT || key == 'd') {
    rightPressed = 1;
  }
}

public void keyReleased() {
  if (keyCode == UP || key == 'w') {
    upPressed = 0;
  }
  else if (keyCode == DOWN || key == 's') {
    downPressed = 0;
  }
  else if (keyCode == LEFT || key == 'a') {
    leftPressed = 0;
  }
  else if (keyCode == RIGHT || key == 'd') {
    rightPressed = 0;
  }
}

public void mousePressed() {
  if (mouseButton == LEFT) {
    lmousedown = true;
  }
  if (mouseButton == RIGHT) {
    rmousedown = true;
  }
}
public void mouseReleased() {
  if (mouseButton == LEFT) {
    lmousedown = false;
  }
  if (mouseButton == RIGHT) {
    rmousedown = false;
  }
}
//does nothing so far because I have not implemented player and entity animation states, particles, graphics settings etc.
public void draw_canvas_layer() {
  canvas.beginDraw();
  position_camera_canvas();
  canvas.background(0);
  for(int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      tiles[j][i].show();
    }
  }
  pportal.show();
  for(int j = 0 ; j < tiles.length; j++) {
    for (int i = 0 ; i < tiles[j].length; i++) {
      if (tiles[j][i].decorations.length>0) {//sort decorations by layer properly
        tiles[j][i].decorations = sortdecorations(tiles[j][i].decorations);
      }
      boolean[] denemydraw = new boolean[enemies.length];
      for (int k = 0 ; k < tiles[j][i].decorations.length; k++) {
        PVector tempdecpos = new PVector(tiles[j][i].decorations[k].pos.x * 128 + tiles[j][i].decorations[k].offset.x, tiles[j][i].decorations[k].pos.y * 128 + tiles[j][i].decorations[k].offset.y + tiles[j][i].decorations[k].frames[0].height);
        for (int l = 0 ; l < enemies.length; l++) {
          PVector enemydims = new PVector(enemies[l].frames[0][0].width/2,enemies[l].frames[0][0].height);
          denemydraw[l] = false;
          if (enemies[l].pos.x + enemydims.x >= j*128 && enemies[l].pos.x + enemydims.x <= j*128+128 && enemies[l].pos.y + enemydims.y >= i*128 && enemies[l].pos.y + enemydims.y <= i*128+128) {//if enemy is in this tile
            if (k + 1 != tiles[j][i].decorations.length) {
              if (enemies[l].pos.y + enemydims.y > tempdecpos.y && enemies[l].pos.y + enemydims.y < tempdecpos.y) {
                denemydraw[l] = true;
              }
            } else if (enemies[l].pos.y + enemydims.y > tempdecpos.y) {
              denemydraw[l] = true;
            } else {
              enemies[l].show();
            }
          }
        }
        if (drudge.pos.x + 64 >= j*128 && drudge.pos.x + 64 <= j*128+128 && drudge.pos.y + 128 >= i*128 && drudge.pos.y + 128 <= i*128+128) {//if player is in this tile
          if (k + 1 != tiles[j][i].decorations.length) {
            if (drudge.pos.y + 128 > tempdecpos.y && drudge.pos.y + 128 < tempdecpos.y) {
              tiles[j][i].decorations[k].show();
              drudge.show();
            }else {
              tiles[j][i].decorations[k].show();
            }
          } else if (drudge.pos.y + 128 > tempdecpos.y) {
            tiles[j][i].decorations[k].show();
            drudge.show();
          } else {
            drudge.show();
            tiles[j][i].decorations[k].show();
          }
        } else {
          tiles[j][i].decorations[k].show();
        }
      }
      for (int l = 0; l < denemydraw.length; l++) {
        PVector enemydims = new PVector(enemies[l].frames[0][0].width/2,enemies[l].frames[0][0].height);
        if (denemydraw[l]) {
          enemies[l].show();
        }
        if (tiles[j][i].decorations.length == 0 && enemies[l].pos.x + enemydims.x >= j*128 && enemies[l].pos.x + enemydims.x <= j*128+128 && enemies[l].pos.y + enemydims.y >= i*128 && enemies[l].pos.y + enemydims.y <= i*128+128) {
          enemies[l].show();
        }
      }
      if (tiles[j][i].decorations.length == 0 && drudge.pos.x + 64 >= j*128 && drudge.pos.x + 64 <= j*128+128 && drudge.pos.y + 128 >= i*128 && drudge.pos.y + 128 <= i*128+128) {
        drudge.show();
      }
    }
    berkely.show();
  }
  if (grid == true) {
    for (int i = 0 ; i < tiles.length; i++) {
      for (int j = 0 ; j < tiles[i].length; j++) {
        canvas.noFill();
        canvas.strokeWeight(1);
        canvas.stroke(0);
        canvas.rect(i*128,j*128,128,128);
      }
    }
  }
  canvas.endDraw();
}
public void draw_litscreen_layer() {
  litscreen.beginDraw();
  litscreen.pushMatrix();
  litscreen.background(0);
  position_camera_litscreen();
  drudge.showshadow();
  pportal.showshadow();//show portal light
  for(int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      if (tiles[j][i].decorations.length>0) {//sort decorations by layer properly
        tiles[j][i].decorations = sortdecorations(tiles[j][i].decorations);
      }
      for (int k = 0 ; k < tiles[j][i].decorations.length; k++) {
        tiles[j][i].decorations[k].showshadow();
      }
    }
  }
  litscreen.popMatrix();
  litscreen.filter(fragshader);//add point lights
  litscreen.fill(255,255,255,time);//add ambient lighting based on time of day
  litscreen.rect(0,0,litscreen.width,litscreen.height);
  litscreen.endDraw();
}
public void draw_postprocessing_layer() {
  postprocessing.beginDraw();
  postprocessing.background(0);
  position_camera_postprocessing();
  for (int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      tiles[i][j].show_postprocessing();
      for (int k = 0 ; k < tiles[j][i].decorations.length; k++) {
        tiles[j][i].decorations[k].show_postprocessing();
      }
    }
  }
  postprocessing.blendMode(MULTIPLY);
  postprocessing.image(perlin,0,0,8000,4000);
  postprocessing.blendMode(NORMAL);
  for (int i = 0 ; i < 100; i++) {
    postprocessing.fill(0,0,0,i);
    postprocessing.ellipse(drudge.pos.x+64 - drudge.vel.x*5,drudge.pos.y+64- drudge.vel.y*5,(postprocessing.width/camera_distance-i*4)/2,(postprocessing.height/camera_distance-i*4)/2);
  }
  pportal.show_postprocessing();//draw portal through fog and other pp effects
  postprocessing.endDraw();
}
int maxlights = 75;
public void handle_lights() {
  PVector mousepos = new PVector(mouseX/camera_distance, mouseY/camera_distance);
  float x = map(mousepos.x, 0, width, 0, 1), y = map(mousepos.y, 0, height, 1, 0);
  lights.setFloat(lights.size(), -100.0f);
  lights.setFloat(lights.size(), 0.0f);
  lights.setFloat(lights.size(), 0.0f);
  lights.setFloat(lights.size(), 0.0f);
  lights.setFloat(lights.size(), 0.0f);
  //convert lights json array to float array
  float[] finallights = new float[min(maxlights,lights.size())];
  for (int i = 0 ; i < min(maxlights,lights.size()); i++) {
    finallights[i] = lights.getFloat(i);
  }
  //send uniforms to shader
  fragshader.set("numLights", PApplet.parseInt(finallights.length/5));
  fragshader.set("reso", sratio);
  fragshader.set("lightscontain", finallights);
}


File[] files;

//this file imports levels
//tiles are stored with the decorations as part of them, so tile.decorations is a list of all decorations in tile
public void loadscene(String url) {
  JSONObject iloaded = new JSONObject();//imported file
  try {
    iloaded = loadJSONObject(url);
    if (true) {//if loaded correctly
      drudge.pos = new PVector(0,0);
      JSONArray itiles = iloaded.getJSONArray("tiles");
      int isize = itiles.size();
      tiles = new tile[(int)sqrt(isize)][(int)sqrt(isize)];
      for (int i = 0 ; i < isize ; i++) {
        JSONObject itile = itiles.getJSONObject(i);//loaded specific tile
        JSONArray jidecorations = itile.getJSONArray("decorations");
        decoration[] idecorations = new decoration[jidecorations.size()];
        tiles[itile.getInt("x")][itile.getInt("y")] = new tile(itile.getInt("x"),itile.getInt("y"),itile.getString("type"));//create new tile with loaded data
        tiles[itile.getInt("x")][itile.getInt("y")].area = itile.getString("area");
        for (int j = 0 ; j < jidecorations.size(); j++) {
          JSONObject jidecoration = jidecorations.getJSONObject(j);
          idecorations[j] = new decoration(itile.getInt("x"),itile.getInt("y"),jidecoration.getFloat("x"),jidecoration.getFloat("y"),jidecoration.getString("type"));//create new decoration with loaded data
          if (idecorations[j].data.getString("name").equals("playerspawn") && drudge.pos.x == 0) {//move player to playerspawn if hasent already moved
            drudge.pos = new PVector(idecorations[j].pos.x*128+idecorations[j].offset.x,idecorations[j].pos.y*128+idecorations[j].offset.y);
          }
        }
        tiles[itile.getInt("x")][itile.getInt("y")].decorations = idecorations;
      }
      JSONArray ienemies = iloaded.getJSONArray("enemies");
      enemies = new enemy[ienemies.size()];
      for (int i = 0 ; i < ienemies.size(); i++) {
        enemies[i] = new enemy(ienemies.getJSONObject(i).getFloat("x"),ienemies.getJSONObject(i).getFloat("y"),ienemies.getJSONObject(i).getString("type"));
      }
    }
    //load meta data like time etc.
    time = iloaded.getInt("time");
  } catch(Exception e) {
    println("Loading failed " + url);
  }
}
int step = 0;
int level = 0;
int lls = 0;//last level switch
int lss = 0;//last stage switch
int levelstage = 0;
String objective = "";
public void play_story() {
  lls++;
  if (step == 0) titletext("(space) to skip", 200);
  if (step == 1) titletext("A traveller crosses paths\nwith someone with a heart\nmade of stone...", 400);
  if (step == 2) titletext("Level: The deep forest river", 250);
  if (step == 3) startobjective("Speak to the Baron Berkely");
  if (step == 4) startlevel("day");//first level
  if (step == 5) titletext("...The traveler becomes\na bounty hunter...", 250);
  if (step == 6) titletext("The megamushroom is\ncontrolled by a knome village", 400);
  if (step == 7) titletext("It gives life to all\nmagical creatures", 350);
  if (step == 8) titletext("Noble unicorns protect\nthis essential resource", 350);
  if (step == 9) titletext("Level: Unicorn blockage", 250);
  if (step == 10) startobjective("Pass through the\nunicorn soldiers safely");
  if (step == 11) startlevel("unicorn");//second level
  if (step == 12) titletext("The knome village\napproaches with the dark", 400);
  if (step == 13) titletext("...Your portal emits light...", 250);
  if (step == 14) titletext("Level: The Megamushroom", 250);
  if (step == 15) startobjective("Capture as many\nknomes as possible\nbefore time runs out");
  if (step == 16) startlevel("mushroomfull");//3rd level
  if (step == 17) titletext("The Megamushroom dies", 250);
  if (step == 18) titletext("All the magical\ncreatures turn normal", 400);
  if (step == 19) titletext("...including yourself...", 250);
  if (step == 20) titletext("The end.", 250);
  if (step == 21) {ingame = false; story = false;};
  if (transition) {//if just transitioning into the game
    transamount++;
    float percent = (float)transamount/((float)height/50.0f);//how close to done animation
    fill(158, 129, 79);
    rect(0,height-height*(percent), width, height);
    if (percent >= 2.0f) {//done animation
      transition = false;
      transamount = 0;
    }
  }
}
public void do_hard_coded() {
  lss++;
  //LEVEL 1
  if (level == 1 && levelstage == 0) {if(lss > 50) nextstage(); berkely.changepos(256,384);}//give a pause and move berkely
  if (level == 1 && levelstage == 1) {berkelyline="Oi! ye! yes ye, the frightening ball of death!"; if(lss > 180) nextstage();}; // berkely talk
  if (level == 1 && levelstage == 2) {//pan to chickencorns in feild
    panto(3372,840,800);
    drudge.pos.set(128*3, 128*3);
    berkelyline="The bridge is blocked and the smelly ol' gatekeeper will not open it until his chickencorns 'r returned.";
    if(lss > 450 && lss < 560) berkelyline="Can ye help me out?";
    if(lss > 560) berkelyline="Those giant stones shouldn't be barney for a chap like yeh, See if you can push 'em outa' the way.";}; // berkely talk
  if (level == 1 && levelstage == 3) {berkelyline="";
    if(PVector.dist(drudge.pos, berkely.pos) < 50) {lss = 0; levelstage = 2;};//if player is unclear they can come near berkely to have him repeat
    if(playerintile(19,3)) nextstage();};
  if (level == 1 && levelstage == 4) {berkely.changepos(1536,640);//move berkely to bridge
    berkelyline="Absolutely brilliant!";//berkely talk
    panto(3372,840,900);
    drudge.pos.set(128*19, 128*3);
    if(lss > 200) berkelyline="Say, you're one of 'em death stalkers ain't you?";
    if(lss > 400) berkelyline="Yea' I remember, you've got that lit'l portal thing you can trap things in.";
    if(lss > 700) berkelyline="Try left clicking for a second on a few of 'em chickencorns";};
  if (level == 1 && levelstage == 5) {berkelyline="";
    if(PVector.dist(drudge.pos, berkely.pos) < 50) {lss = 0; levelstage = 4;};//if player is unclear they can come near berkely to have him repeat
    if(entitiesinportal() >= 5) nextstage();}; // if there are more than 5 entites in the portal
  if (level == 1 && levelstage == 6) {berkelyline="Brilliant!";//berkely talk
    panto(17*128,6*128,750);
    drudge.pos.set(128*19, 128*3);//move to where boulder was moved
    if(lss > 200) {berkelyline="Now ye can hold right click over that now chickenpen.";};
    if(lss > 400) berkelyline="I'll be on the other side of the river in no time!";
    if(lss > 600) berkelyline="Err... I mean WE will of course!";};
  if (level == 1 && levelstage == 7) {berkelyline="";
    if(PVector.dist(drudge.pos, berkely.pos) < 50) {lss = 0; levelstage = 6;};//if player is unclear they can come near berkely to have him repeat
    checkentities(1280,384,1664,512,5);};//if there are at least five chickencorns in the pen, nextstage()
  if (level == 1 && levelstage == 8) {berkelyline="";
    panto(13*192,7*192,150);//pan to open bridge
    berkely.changepos(1850,1100); // move berkely
    settile(13,6,"grass");settile(14,6,"grass");settile(12,6,"stone");settile(15,6,"stone");};//open river passage
  if (level == 1 && levelstage == 9) {berkelyline="";
    if (playerintile(13,8) || playerintile(14,8)) nextstage();}; // when player crosses river
  if (level == 1 && levelstage == 10) {berkelyline="Oh how unfortunate,";
    drudge.pos.set(128*13, 128*9);//set player pos to ofter river
    berkely.changepos(1850,1000);
    panto(12*192,13*192,300);//pan to closed gateway
    if(lss > 100) berkelyline="Looks as though the river this side is also blocked...";};
  if (level == 1 && levelstage == 11) {berkelyline="You're going to hav 'ta concoct some way to get past 'ose boulders";//say this without pan
    drudge.pos.set(128*13, 128*9);//set player pos to river
    if (lss > 275) nextstage();};
  if (level == 1 && levelstage == 12) {berkelyline="";//await playing entering foggy forest
    if (playerintile(20,9) || playerintile(21,13)) {nextstage();};//if player in foggy forest
    if(PVector.dist(drudge.pos, berkely.pos) < 50) {lss = 0; levelstage = levelstage-=2;};};//if player near berkely repeat line
  if (level == 1 && levelstage == 13) {berkelyline="Brilliant!";
    drudge.pos.set(128*21, 128*10);//set player pos to entering forest
    panto(21*192,9*192,500);//pan over forest
    if(lss > 100) {berkelyline="Those glowing blue rocks work as switches!";};
    if(lss > 300) berkelyline="If you put a boulder on each switch, the path should clear!";};
  if (level == 1 && levelstage == 14) {berkelyline="I'm sure yea'll be able to scour up some rocks somewhere...";
    panto(7*192,10*192,300);//pan over other side of forest
    drudge.pos.set(128*21, 128*10);};
  if (level == 1 && levelstage == 15) {berkelyline="";
    if(PVector.dist(drudge.pos, berkely.pos) < 50) {lss = 0; levelstage = levelstage-=2;};//replay cutscene if player gets stuck
    if(tileat(25,10).equals("stone") && tileat(27,10).equals("stone")) {nextstage();};};
  if (level == 1 && levelstage == 16) {berkelyline="Brilliant work!";
    panto(12*192,13*192,100);//show open gateway
    settile(11,16,"grass");settile(12,16,"grass");settile(13,16,"grass");//open gateway
    berkely.changepos(13*128,16*128);};//move berkely to gateway
  if (level == 1 && levelstage == 17) {berkelyline="";
    if(playerintile(11,16)||playerintile(12,16)||playerintile(13,16)){nextstage();}};//if player moves to last section
  if (level == 1 && levelstage == 18) {drudge.pos.set(128*11, 128*16);
    berkelyline="Say, you'd make a particularly fine bounty hunter...";
    if(lss > 250) berkelyline="Yea know, my company specializes in the errr...";
    if(lss > 600) berkelyline="removal... of magical creatures";
    if(lss > 800) berkelyline="You wouldn't be interested, would you?";
    if(lss > 1000) berkelyline="Oh, and did I mention there would be a pretty coin involved?";
    if(lss > 1200) nextstage();};
  if (level == 1 && levelstage == 19) {berkelyline="ahh, here comes a flock of chickencorns now, think you can prove yourself? Catch 10.";
    panto(10*192,16*192,300);}
  if (level == 1 && levelstage == 20) {berkelyline="";
    if(PVector.dist(drudge.pos, berkely.pos) < 20) {lss = 0; levelstage = levelstage-=2;}//if close to berkely repeat cutscene
    if(entitiesinportal() >=10) {nextstage();settile(14,21,"grass");settile(14,22,"grass");};};//if 10 chickencorns in portal
  if (level == 1 && levelstage == 21) {berkelyline="Ludicrously fast!";
    panto(13*192,17*192,300);
    berkely.changepos(14*128,21*128);}
  if (level == 1 && levelstage == 22) {ingame=false;nextstep();};//level finished
  //LEVEL 2
  if (level == 2 && levelstage == 0) {if(lss > 50) nextstage(); berkely.changepos(256,384);}//give a pause and move berkely
  if (level == 2 && levelstage == 1) {berkelyline="The Megamushroom is whatsa we're after."; if(lss > 180) nextstage();};
  if (level == 2 && levelstage == 2) {berkelyline="Noone wants a unicorn, but we can sell a horse for twice the price!"; if(lss > 230) nextstage();};
  if (level == 2 && levelstage == 3) {berkelyline="";
    if(playerintile(17,3)||playerintile(18,3)||playerintile(19,3)){ingame=false;nextstep();};};//if player finish level
  //LEVEL 3
  if (level == 3 && levelstage == 0) {if(lss > 400) {nextstage();};
    berkelyline="Those ol' knomes control the Megamushroom, we destr... move them an' I win.";};//give a pause and move berkely
  if (level == 3 && levelstage == 1) {berkelyline="";
    messagetoplayer = ""+((6000.0f-(float)lss))/60.0f;//a minute 30 seconds
    ismessagetoplayer = true;
    added = width-70;
    if(((6000.0f-(float)lss))/60.0f <= 0) {ismessagetoplayer = false; nextstage();};};
  if (level == 3 && levelstage == 2) {
    ingame=false;
    nextstep();
    added = 0;};
  //if (level == 1 && levelstage == 4) { if(lss > 60) nextstage();}; // berkely talk
  //if (level == 1 && levelstage == 3) checkentities(1280,384,1664,512,5);//if 5 entities are in the chickenpen
  //if (level == 1 && levelstage == 4) {settile(13,6,"grass");settile(14,6,"grass");settile(12,6,"leaves");settile(15,6,"leaves");nextstage();};//open river passage
}
float added = 0;
public void titletext(String title, int delay) {
  background(0);
  textAlign(CENTER,CENTER);
  textSize(70 + min(14,lls/4));
  fill(255);
  text(title,width/2,height/2);
  if (lls == delay || skip) nextstep();
}
public void nextstep() {
  skip = false;
  step++;
  lls = 0;
}
public void nextstage() {
  levelstage++;
  lss = 0;
}
public void die() {
  isdying = true;
}
public void respawn() {
  PVector spawnpoint = new PVector(256,256);
  //find the player spawnpoint
  for (int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      if (spawnpoint.x == 256 && spawnpoint.y == 256) {
        for (int k = 0 ; k < tiles[i][j].decorations.length; k++) {
          if (tiles[i][j].decorations[k].data.getString("name").equals("playerspawn")) {
            spawnpoint.x = i*128;spawnpoint.y = j*128;
          }
        }
      }
    }
  }
  drudge.pos.set(spawnpoint.x,spawnpoint.y);//move player to spawn point
  if (story) {//if in story restart level
    ingame = false;
    levelstage = 0;
    lls = 0;
    lss = 0;
    step-=1;
  }
}
public void startlevel(String path) {
  loadscene("scenes/"+path+".json");
  levelstage = 0;
  level++;
  transition = false;
  transamount = 0;
  ingame = true;
}
public void startobjective(String text) {
  titletext("Objective:\n"+text, 250);
  objective = text;
}

public void checkentities(float x, float y, float x1, float y1, int amount) {
  int numentites = 0;
  for (int i = 0 ; i < enemies.length; i++) {
    PVector thispos = new PVector(enemies[i].pos.x, enemies[i].pos.y);
    if (thispos.x > x && thispos.x < x1 && thispos.y > y && thispos.y < y1 && enemies[i].dead == false) {//if inbetween invisible box of (x,y) and (x1,y1)
      numentites++;
    }
  }
  if (numentites >= amount) nextstage();
}
public int entitiesinportal() {
  int total = 0;
  for (int i = 0 ; i < enemies.length; i++) {
    if (enemies[i].dead) {
      total++;
    }
  }
  return total;
}
public String tileat(int x, int y) {
  return tiles[x][y].data.getString("name");
}
public void settile(int x, int y, String type) {
  tiles[x][y] = new tile(x, y, type);
}
public boolean playerintile(float x, float y) {
  if (drudge.tilepos.x == x && drudge.tilepos.y == y) return true;
  return false;
}
PVector cutamount = new PVector(0,0);
PVector prevdir = new PVector(0,0);
public void panto(float x_, float y_, int holdtime) {//pan to position on screen for given time
  int delay = 120;
  float x = (x_-width/2-(drudge.pos.x/camera_distance))*camera_distance;
  float y = (y_-height/2-(drudge.pos.y/camera_distance))*camera_distance;
  cutscene = true;
  PVector thispos = new PVector(x,y);
  PVector dir = PVector.add(thispos,camera_position);
  if (lss < delay) {//on the way to locationS
    dir.mult(min(abs((float)lss)/abs((float)delay), 1));
    cutamount = dir;
  } else if (lss < delay+holdtime) {//while at location
    if (dir.x < prevdir.x + 128 && dir.x > prevdir.x - 128 && dir.y < prevdir.y + 128 && dir.y > prevdir.y - 128) {//prevent shaking
      dir = prevdir;
    }
    cutamount = dir;
  } else if (lss > delay+holdtime) {//coming back from location
    dir.mult(max(abs(1-abs((((float)lss-(float)delay-holdtime))/abs((float)delay))),0));
    cutamount = dir;
  }
  prevdir = dir;
  if (lss == delay*2+holdtime) {cutscene = false; nextstage();println("finishpan");};
}
//the player and entities class

class entity {
  PVector pos;
  PVector vel;
  JSONObject data;
  String[] animation_types;
  PImage[][] frames;
  String[][] dframes;
  String animation_machine;
  float speed = 15;
  int curframe = 0;
  public void createframes() {
    dframes = new String[animation_types.length][data.getJSONArray(animation_types[0]).size()];
    frames = new PImage[animation_types.length][data.getJSONArray(animation_types[0]).size()];
    for (int k = 0 ; k < animation_types.length; k++) {
      int index = -1;
      dframes[k] = new String[data.getJSONArray(animation_types[k]).size()];
      frames[k] = new PImage[dframes.length];
      for (int i = 0 ; i < data.getJSONArray(animation_types[k]).size(); i++) {
        index = -1;
        for (int j = 0 ; j < dtileimages.length; j++) {
          if (dtileimages[j].equals(data.getJSONArray(animation_types[k]).getString(i))) {
            index = j;
          }
        }
        if (index == -1) {
          println("Loading textures for entity");
          dframes[k][i] = data.getJSONArray(animation_types[k]).getString(i);
          println("\tLoaded " + dframes[k][i]);
          frames[k][i] = loadImage(dframes[k][i], "png");
          PImage[] temptileimages = tileimages;
          String[] tempdtileimages = dtileimages;
          tileimages = new PImage[tempdtileimages.length+1];
          dtileimages = new String[tempdtileimages.length+1];
          for (int j = 0 ; j < tempdtileimages.length; j++) {
            tileimages[j] = temptileimages[j];
            dtileimages[j] = tempdtileimages[j];
          }
          dtileimages[dtileimages.length-1] = dframes[k][i];
          tileimages[tileimages.length-1] = frames[k][i];
        } else {
          frames[k][i] = tileimages[index];
        }
          curframe = 0;//current frame
          speed = data.getInt("speed");//animation speed
      }
    }
  }
}

class portal extends entity {//goes to mouse pointer, captures enemies, emits light
  portal(float x, float y) {
    pos = new PVector(x,y);
    data = loadJSONObject("player.json");
    String[] portaltypes = {"portal","portal","portal"};
    animation_types = portaltypes;
    animation_machine = "portal";
    createframes();
  }
  public void show() {
    canvas.noStroke();
    if (lmousedown) {
      canvas.tint(255,0,50);
    } else if (rmousedown) {
      canvas.tint(0,50,50);
    }
    if (rmousedown || lmousedown) {
      canvas.image(frames[0][curframe],pos.x, pos.y);
    }
    canvas.noTint();
  }
  public void showshadow() {//draw purple light
    if (lmousedown || rmousedown) {
      lights.setFloat(lights.size(),map(((pos.x+32+camera_position.x)*camera_distance)/width,0,1,0,0.5f));
      lights.setFloat(lights.size(),map(1-((pos.y+12+camera_position.y)*camera_distance)/height,0,1,0.5f,1));
      lights.setFloat(lights.size(),(float)30/(float)255);
      lights.setFloat(lights.size(),(float)5/(float)255);
      lights.setFloat(lights.size(),(float)100/(float)255);
    }
  }
  public void show_postprocessing() {
    if (rmousedown || lmousedown) {
      PVector shift = new PVector((float)frames[0][0].width,(float)frames[0][0].height);//draw fading ellipse around portal
      for (int i = 0 ; i < 100; i+=2) {
        postprocessing.fill(0,0,0,100-i);
        postprocessing.ellipse(pos.x+shift.x/2,pos.y+shift.y/2,shift.x+i,shift.y+i);
      }
    }
  }
  public void update() {
    pos = new PVector((mouseX/camera_distance-camera_position.x)-32, (mouseY/camera_distance-camera_position.y)-12);
    //check entity collisions
    if (lmousedown) {
      for (int i = 0 ; i < enemies.length; i++) {
        if (enemies[i].pos.dist(PVector.add(pos, new PVector(frames[0][0].width/2,frames[0][0].height/2))) < 32 && !enemies[i].data.getString("behave").equals("charge")) { //if enemy is close to portal
          enemies[i].death();//ping enemy every frame they are too near
        }
      }
    }
    if (rmousedown && frameCount%60 == 0) {
      for (int i = 0 ; i < enemies.length; i++) {
        if (enemies[i].dead) { //if enemy dead then revive at portal
          enemies[i].revive();
        }
      }
    }
  }
}

class enemy extends entity {
  PVector target;
  int deathamount = 0;
  boolean deathamountchange = false;
  boolean dead = false;
  float size = 0;
  enemy(float x, float y, String type) {
    pos = new PVector(x,y);
    vel = new PVector(0,0);
    target = new PVector(x,y);
    data = denemys.getJSONObject(type);
    String[] playertypes = {"right","left","idle"};
    for (int i = 0 ; i < playertypes.length; i++) {
      if (data.isNull(playertypes[i])) {
        playertypes = removestringfrom(playertypes, i);
        i--;
      }
    }
    animation_types = playertypes;
    animation_machine = "idle";
    createframes();
  }
  public void show() {
    deathamountchange = false;
    if (!dead) {
      if (frameCount % speed == 0) {
        curframe++;
      }
      curframe = curframe%data.getJSONArray("idle").size();
      canvas.noStroke();
      int index = 0;
      for (int i = 0 ; i < animation_types.length; i++) {
        if (animation_types[i] == animation_machine) {
          index = i;
        }
      }
      canvas.image(frames[index][curframe],pos.x, pos.y, frames[index][curframe].width/(size+1), frames[index][curframe].height/(size+1));
    }
  }
  public void update() {
    if (!dead) {
      size = (float)deathamount/(float)30;//if 100 frames go by while over portal, kill entity
      if (deathamount == 0) {
        if (data.getString("behave").equals("random")) {
          randompath();
        }
        if (data.getString("behave").equals("charge")) {
          charge();
        }
      } else if (deathamountchange) {
        if (size >= 1) {
          dead = true;
        }
      } else {
        deathamount = floor(deathamount * 0.8f);
      }
      //check tile cols
      for (int i = 0 ; i < tiles.length; i++) {
        for (int j = 0 ; j < tiles[i].length; j++) {
          if (PVector.mult(tiles[i][j].pos,128).dist(pos) < 512) { // if reasonable distance from tile check cols
            if (tiles[i][j].data.getString("wall").equals("true")) {
              if(tiles[i][j].checkcol(PVector.add(pos,new PVector(frames[0][0].width/2,frames[0][0].height/2)))) {
                PVector tilecenter = PVector.add(new PVector(pos.x+frames[0][0].width/2,pos.y+frames[0][0].height/2),vel);
                PVector playercenter = new PVector(pos.x+frames[0][0].width/2,pos.y+frames[0][0].height/2);
                PVector difference = PVector.sub(playercenter,tilecenter);
                if (data.getString("behave").equals("charge")) {
                  direction = !direction;
                  difference.setMag(40);
                } else {
                  difference.setMag(5);
                }
                vel.add(difference);
              }
            }
          }
        }
      }
      //check decoration cols
      for (int i = 0 ; i < tiles.length; i++) {
        for (int j = 0 ; j < tiles[i].length; j++) {
          if (PVector.mult(tiles[i][j].pos,128).dist(pos) < 512) { // if reasonable distance from tile check cols
            for (int k = 0 ; k < tiles[i][j].decorations.length; k++) {
              if (tiles[i][j].decorations[k].data.getString("wall").equals("true")) {
                if (tiles[i][j].decorations[k].checkcol(PVector.add(pos,new PVector(frames[0][0].width/2,frames[0][0].height/2)))) {
                  PVector decoffset = tiles[i][j].decorations[k].offset;
                  PVector decdims = new PVector(tiles[i][j].decorations[k].frames[0].width,tiles[i][j].decorations[k].frames[0].height);
                  PVector deccenter = PVector.add(new PVector(pos.x+frames[0][0].width/2,pos.y+frames[0][0].height/2),vel);
                  PVector playercenter = new PVector(pos.x+frames[0][0].width/2,pos.y+frames[0][0].height/2);
                  PVector difference = PVector.sub(playercenter,deccenter);
                  if (data.getString("behave").equals("charge")) {
                    direction = !direction;
                    difference.setMag(40);
                  } else {
                    difference.setMag(5);
                  }
                  vel.add(difference);
                }
              }
            }
          }
        }
      }
      vel.mult(0.8f);
      if (vel.mag() < 0.4f) {
        vel.setMag(0);
      }
      pos.add(vel);
      //change animation
      if (vel.x > 1) {
        animation_machine = "right";
      } else if (vel.x < -1) {
        animation_machine = "left";
      }
    } else {//if dead
      pos.x = pportal.pos.x;
      pos.y = pportal.pos.y;
    }
  }
  public void revive() {
    dead = false;
  }
  public void death() {
    deathamount++;
    deathamountchange = true;
  }
  public void randompath() {//chickencorns and knomes
    if (frameCount%120 == 0) {
      target = PVector.random2D();
      target.setMag(128);
      target.add(pos);
    }
    PVector difference = PVector.sub(target,pos);
    difference.setMag(data.getFloat("movespeed"));
    vel.add(difference);
  }
  boolean direction = true;
  public void charge() {//unicorns
    float multiplier = 1;
    if (drudge.pos.y + 64 > pos.y - 30 && drudge.pos.y + 64 < pos.y + 128) {
      multiplier = 4;
    }
    PVector difference;
    if (direction) {
      difference = new PVector(1,0);
    } else {
      difference = new PVector(-1,0);
    }
    difference.setMag(data.getFloat("movespeed")*multiplier);
    vel.add(difference);
  }
}

class player extends entity {
  float maxspeed = 6;
  PVector tilepos = new PVector(0,0);
  float acceleration = 0.25f;
  player(float x, float y) {
    String[] playertypes = {"right","left","idle","righttrans","lefttrans"};
    animation_types = playertypes;
    animation_machine = "idle";
    pos = new PVector(x,y);
    vel = new PVector(0,0);
    data = loadJSONObject("player.json");//player json data/player.json
    createframes();
  }
  public void show() {
    curframe = curframe%3;
    if (frameCount % speed == 0) {
      curframe++;
    }
    canvas.noStroke();
    int index = 0;
    for (int i = 0 ; i < animation_types.length; i++) {
      if (animation_types[i] == animation_machine) {
        index = i;
      }
    }
    canvas.image(frames[index][curframe],pos.x, pos.y-(frames[index][curframe].height-128));
  }
  public void showshadow() {
    int index = 0;
    for (int i = 0 ; i < animation_types.length; i++) {
      if (animation_types[i] == animation_machine) {
        index = i;
      }
    }
    litscreen.image(frames[index][curframe],pos.x/quality, pos.y/quality-(frames[index][curframe].height/quality-128/quality),frames[index][curframe].width/quality,frames[index][curframe].height/quality);
  }
  public void update() {
    tilepos = new PVector(floor(pos.x/128),floor(pos.y/128));//set tile pos to which tile the player is in
    PVector movedir = new PVector(0,0);
    if (!cutscene) {
      if (downPressed == 1) {
        movedir.y++;
        animation_machine = "idle";
      }
      if (upPressed == 1) {
        movedir.y--;
        animation_machine = "idle";
      }
      if (leftPressed == 1) {
        movedir.x--;
        animation_machine = "left";
      }
      if (rightPressed == 1) {
        movedir.x++;
        animation_machine = "right";
      }
      if (vel.mag() < 1.5f && movedir.x == 0 && movedir.y == 0) {
        animation_machine = "idle";
      }
      movedir.setMag(acceleration);
      vel.add(movedir);
      vel.limit(maxspeed);
      pos.add(vel);
    }
    if (movedir.x == 0 && movedir.y == 0) {//if not pressing move buttons lose 10% of speed
      vel.mult(0.9f);
    }
    //check entity collisions
    for (int i = 0 ; i < enemies.length; i++) {
      if (enemies[i].data.getString("behave").equals("charge")) {//if this enemy is dangerous to touch
        if ((new PVector(drudge.pos.x+64,drudge.pos.y+90)).dist((new PVector(enemies[i].pos.x+64,enemies[i].pos.y+64))) < 50) {//if this enemy is within certain range
          die();//(levelcontroller.pde)
        }
      }
    }
    //check tile cols
    for (int i = 0 ; i < tiles.length; i++) {
      for (int j = 0 ; j < tiles[i].length; j++) {
        if (PVector.mult(tiles[i][j].pos,128).dist(pos) < 512) { // if reasonable distance from tile check cols
          if (tiles[i][j].iswall()) {
            if(tiles[i][j].checkcol(PVector.add(pos,new PVector(64,128)))) {
              if (tiles[i][j].ispush()) {
                PVector tilecenter = PVector.add(PVector.add(pos,vel),new PVector(64,128));
                PVector playercenter = new PVector(pos.x+64,pos.y+128);
                PVector difference = PVector.sub(playercenter,tilecenter);
                difference = new PVector(difference.y, -difference.x);
                if (!tiles[i][j].pushwall()) {//if this wall cant move
                  difference.setMag(15);
                  vel.add(difference);
                } else {
                  difference.setMag(1);
                  vel.add(difference);
                }
              } else {
                PVector tilecenter = PVector.add(PVector.add(pos,vel),new PVector(64,128));
                PVector playercenter = new PVector(pos.x+64,pos.y+128);
                PVector difference = PVector.sub(playercenter,tilecenter);
                difference = new PVector(difference.y, -difference.x);
                difference.setMag(15);
                vel.add(difference);
              }
            }
          }
        }
      }
    }
    //check decoration cols
    for (int i = 0 ; i < tiles.length; i++) {
      for (int j = 0 ; j < tiles[i].length; j++) {
        if (PVector.mult(tiles[i][j].pos,128).dist(pos) < 512) { // if reasonable distance from tile check cols
          for (int k = 0 ; k < tiles[i][j].decorations.length; k++) {
            if (tiles[i][j].decorations[k].iswall()) {//if the decoration is a wall
              if (tiles[i][j].decorations[k].checkcol(PVector.add(pos,new PVector(64,128)))) {
                PVector decoffset = tiles[i][j].decorations[k].offset;
                PVector decdims = new PVector(tiles[i][j].decorations[k].frames[0].width,tiles[i][j].decorations[k].frames[0].height);
                PVector deccenter = PVector.add(PVector.add(pos,vel),new PVector(64,128));
                PVector playercenter = new PVector(pos.x+64,pos.y+128);
                PVector difference = PVector.sub(playercenter,deccenter);
                difference = new PVector(difference.y, -difference.x);
                difference.setMag(15);
                vel.add(difference);
              }
            }
            if (tiles[i][j].decorations[k].iscoin()) {//if is a coin
              if (tiles[i][j].decorations[k].checkcol(PVector.add(pos,new PVector(64,128)))) {//if touching the coin
                gotcoin();//(uicontroller.pde)
                //remove coin decoration
                decoration[] tempdecs = tiles[i][j].decorations;
                decoration[] decorations = new decoration[tempdecs.length-1];
                if (tempdecs.length > 0) {
                  for (int l = 0 ; l < tempdecs.length-1; l++) {
                    if (l != k) {
                      decorations[decorations.length] = tempdecs[l];
                    }
                  }
                }
                tiles[i][j].decorations = decorations;
              }
            }
          }
        }
      }
    }
  }
}
//loads json textures

public void load_json() {
  //load all tile json
  tilemanifest = loadJSONArray("tiles/manifest.json");
  println("Loaded tiles tile manifest");
  dtiles = new JSONObject();
  for (int i = 0 ; i < tilemanifest.size(); i++) {
    JSONObject dtile = loadJSONObject("tiles/"+tilemanifest.getString(i)+".json");
    println("\t-Loaded " + tilemanifest.getString(i) + ".json");
    dtiles.setJSONObject(tilemanifest.getString(i),dtile);
  }
  //load all decoration json
  decorationmanifest = loadJSONArray("decorations/manifest.json");
  println("Loaded tiles decorations manifest");
  ddecorations = new JSONObject();
  for (int i = 0 ; i < decorationmanifest.size(); i++) {
    JSONObject ddecoration = loadJSONObject("decorations/"+decorationmanifest.getString(i)+".json");
    println("\t-Loaded " + decorationmanifest.getString(i) + ".json");
    ddecorations.setJSONObject(decorationmanifest.getString(i),ddecoration);
  }
  //load all area json
  areamanifest = loadJSONArray("areas/manifest.json");
  println("Loaded area manifest");
  dareas = new JSONObject();
  for (int i = 0 ; i < areamanifest.size(); i++) {
    JSONObject darea = loadJSONObject("areas/"+areamanifest.getString(i)+".json");
    println("\t-Loaded " + areamanifest.getString(i) + ".json");
    dareas.setJSONObject(areamanifest.getString(i),darea);
  }
  //load enemies
  enemymanifest = loadJSONArray("enemies/manifest.json");
  println("Loaded enemy manifest");
  denemys = new JSONObject();
  for (int i = 0 ; i < enemymanifest.size(); i++) {
    JSONObject denemy = loadJSONObject("enemies/"+enemymanifest.getString(i)+".json");
    println("\t-Loaded " + enemymanifest.getString(i) + ".json");
    denemys.setJSONObject(enemymanifest.getString(i),denemy);
  }
  allmanifest[0] = tilemanifest;
  allmanifest[1] = decorationmanifest;
  allmanifest[2] = areamanifest;
  allmanifest[3] = enemymanifest;
}
class tile extends object {
  PVector pos;
  String type;
  int layer;
  String area = "none";
  decoration[] decorations;
  tile(int x, int y, String _type) {
    decorations = new decoration[0];
    pos = new PVector(x,y);
    String type = _type;
    data = dtiles.getJSONObject(type);
    createframes();
  }
  public void newdecoration(float x, float y, String type) {
    decoration thisdecoration = new decoration((int)pos.x, (int)pos.y, x, y, type);
    decoration[] tempdecs = decorations;
    decorations = new decoration[tempdecs.length+1];
    if (tempdecs.length > 0) {
      for (int i = 0 ; i < decorations.length-1; i++) {
        decorations[i] = tempdecs[i];
      }
    }
    decorations[decorations.length-1] = thisdecoration;
    println("created decor");
  }
  public void show() {
    animate();
    if (hastexture) {
      canvas.noStroke();
      canvas.image(frames[curframe],128*pos.x, 128*pos.y-(frames[curframe].height-128));
      if (area != "none") {
        JSONObject thisarea = dareas.getJSONObject(area);
        if (show_areas) {
          int[] colors = new int[4];
          for (int i = 0 ; i < 4; i++) {
            colors[i] = thisarea.getJSONArray("color").getInt(i);
          }
          canvas.fill(colors[0],colors[1],colors[2],colors[3]);
          canvas.rect(128*pos.x, 128*pos.y,128,128);
        }
      }
    }
  }
  public void show_postprocessing() {
    if (!area.equals("none")) {
      JSONObject thisarea = dareas.getJSONObject(area);
      int[] colors = new int[4];
      for (int i = 0 ; i < 4; i++) {
        colors[i] = thisarea.getJSONArray("color").getInt(i);
      }
      postprocessing.fill(colors[0]*max(0.2f,time/255),colors[1]*max(0.2f,time/255),colors[2]*max(0.2f,time/255),colors[3]);
      postprocessing.noStroke();
      postprocessing.ellipse(pos.x*128+64,pos.y*128+64,512,512);
    }
  }
  public boolean ispush() {
    if (data.getString("moveable").equals("true")) {
      return true;
    }
    return false;
  }
  public boolean pushwall() {
    PVector otherpos = new PVector(drudge.pos.x,drudge.pos.y);
    otherpos.add(new PVector(64,128));
    otherpos.sub(PVector.mult(drudge.vel,5));
    int thisx = PApplet.parseInt(pos.x);
    int thisy = PApplet.parseInt(pos.y);
    if (otherpos.x < thisx*128) {//being pushed right
      if (tiles[thisx+1][thisy].data.getString("name").equals(data.getString("after_move_tile"))) {
        tiles[thisx+1][thisy] = new tile(thisx+1,thisy,data.getString("name"));
        tiles[thisx][thisy] = new tile(thisx, thisy, data.getString("after_move_tile"));
        return true;
      }
    }
    if (otherpos.x > thisx*128 + 128) {//being pushed left
      if (tiles[thisx-1][thisy].data.getString("name").equals(data.getString("after_move_tile"))) {
        tiles[thisx-1][thisy] = new tile(thisx-1,thisy,data.getString("name"));
        tiles[thisx][thisy] = new tile(thisx, thisy, data.getString("after_move_tile"));
        return true;
      }
    }
    if (otherpos.y < thisy*128) {//being pushed down
      if (tiles[thisx][thisy+1].data.getString("name").equals(data.getString("after_move_tile"))) {
        tiles[thisx][thisy+1] = new tile(thisx,thisy+1,data.getString("name"));
        tiles[thisx][thisy] = new tile(thisx, thisy, data.getString("after_move_tile"));
        return true;
      }
    }
    if (otherpos.y > thisy*128 + 128) {//being pushed up
      if (tiles[thisx][thisy-1].data.getString("name").equals(data.getString("after_move_tile"))) {
        tiles[thisx][thisy-1] = new tile(thisx,thisy-1,data.getString("name"));
        tiles[thisx][thisy] = new tile(thisx, thisy, data.getString("after_move_tile"));
        return true;
      }
    }
    return false;
  }
  public boolean iswall() {
    if (data.getString("wall").equals("true")) {
      return true;
    }
    return false;
  }
  public boolean checkcol(PVector point_) {
    PVector point = point_;
    if (point.x > pos.x*128 && point.y > pos.y*128 && point.x < pos.x*128 + 128 && point.y < pos.y*128 + 128) {
      return true;
    }
    return false;
  }
}

class decoration extends object {
  PVector pos;
  PVector offset;
  String type;
  int layer;
  decoration(int x, int y, float x1, float y1, String _type) {
    pos = new PVector(x,y);
    offset = new PVector(x1,y1);
    String type = _type;
    data = ddecorations.getJSONObject(type);
    createframes();
  }
  public void show() {
    if (hastexture) {
      animate();
      canvas.noStroke();
      canvas.image(frames[curframe],128*pos.x+offset.x, 128*pos.y+offset.y);
    }
  }
  public void showshadow() {
    if (data.getString("blockslight").equals("true")) {
      litscreen.image(frames[curframe],(128*pos.x+offset.x)/quality, (128*pos.y-(frames[curframe].height-128)+offset.y)/quality,frames[curframe].width/quality,frames[curframe].height/quality);
    }
    if (!data.isNull("light") && PVector.dist(PVector.mult(pos,128), drudge.pos) < 512) {//this decoration emits light
      lights.setFloat(lights.size(),map(((128*pos.x+offset.x)*camera_distance+camera_position.x*camera_distance)/width,0,1,0,0.5f));
      lights.setFloat(lights.size(),map(1-((128*pos.y+offset.y)*camera_distance+camera_position.y*camera_distance)/height,0,1,0.5f,1));
      int[] colors = new int[3];
      for (int i = 0 ; i < colors.length; i++) {
        lights.setFloat(lights.size(),(float)data.getJSONArray("light").getInt(i)/(float)255);
      }
    }
  }
  public void show_postprocessing() {
    if (!data.isNull("light") && PVector.dist(PVector.mult(pos,128), drudge.pos) < 512) {
      PVector shift = new PVector(64,64);
      if (hastexture) {
        shift = new PVector((float)frames[0].width,(float)frames[0].height);
      }
      for (int i = 0 ; i < 100; i+=2) {
        postprocessing.fill(0,0,0,100-i);
        postprocessing.ellipse(128*pos.x+offset.x,128*pos.y+offset.y,shift.x+i,shift.y+i);
      }
    }
  }
  public boolean checkcol(PVector point_) {
    PVector point = point_;
    if (hastexture) {
      if (point.x > pos.x*128+offset.x && point.y > pos.y*128+offset.y + frames[0].height/2 && point.x < pos.x*128 +offset.x + frames[0].width && point.y < pos.y*128 +offset.y + frames[0].height) {
        return true;
      }
    }
    return false;
  }
  public boolean iscoin() {
    if (!data.isNull("coin")) {
      return true;
    }
    return false;
  }
  public boolean iswall() {
    if (data.getString("wall").equals("true")) {
      return true;
    }
    return false;
  }
}

public decoration[] sortdecorations(decoration[] decs_) {//sort decorations by draw order
  decoration[] decs = decs_;
  decoration[] tempdecs = new decoration[decs.length];
  while(decs.length > 0) {
    float low = 1000;
    int index = -1;
    for (int i = 0 ; i < decs.length; i++) {
      if (decs[i].offset.y < low) {
        low = decs[i].offset.y;
        index = i;
      }
    }
    decoration[] newdecs = new decoration[decs.length-1];
    boolean found = false;
    for (int i = 0 ; i < decs.length; i++) {
      if (i != index) {
        if (!found) {
          newdecs[i] = decs[i];
        } else {
          newdecs[i-1] = decs[i];
        }
      } else {
        found = true;
      }
    }
    tempdecs[tempdecs.length - decs.length] = decs[index];
    decs = newdecs;
  }
  return tempdecs;
}

class object {
  PImage[] frames;
  String[] dframes;
  int curframe;
  int speed;
  boolean hastexture = true;
  JSONObject data;
  public void createframes() {
    if (data.getJSONArray("frames").size() == 0) {
      hastexture = false;
    } else {
      int index = -1;
      dframes = new String[data.getJSONArray("frames").size()];
      frames = new PImage[dframes.length];
      for (int i = 0 ; i < data.getJSONArray("frames").size(); i++) {
        index = -1;
        for (int j = 0 ; j < dtileimages.length; j++) {
          if (dtileimages[j].equals(data.getJSONArray("frames").getString(i))) {
            index = j;
          }
        }
        if (index == -1) {
          println("Loading texture for " + data.getString("name"));
          dframes[i] = data.getJSONArray("frames").getString(i);
          println("\tLoaded " + dframes[i]);
          frames[i] = loadImage(dframes[i], "png");
          PImage[] temptileimages = tileimages;
          String[] tempdtileimages = dtileimages;
          tileimages = new PImage[tempdtileimages.length+1];
          dtileimages = new String[tempdtileimages.length+1];
          for (int j = 0 ; j < tempdtileimages.length; j++) {
            tileimages[j] = temptileimages[j];
            dtileimages[j] = tempdtileimages[j];
          }
          dtileimages[dtileimages.length-1] = dframes[i];
          tileimages[tileimages.length-1] = frames[i];
        } else {
          frames[i] = tileimages[index];
        }
        curframe = 0;//current frame
        speed = data.getInt("speed");//animation speed
      }
    }
  }
  public void animate() {
    if (speed != 0) {
      if (frameCount % speed == 0) {
        curframe++;
        curframe = curframe%dframes.length;
      }
    }
  }
}
//misc input/output and ui

boolean grid = false;//grid for debuging

public String removecharat(int index, String input) {
  String newstring = "";
  for (int i = 0 ; i < input.length(); i++) {
    if (i != index) {
      newstring += input.charAt(i);
    }
  }
  return newstring;
}

public String[] removestringfrom(String[] tempstrings_, int index) {
  String[] tempstrings = tempstrings_;
  ArrayList<String> tempremoved = new ArrayList<String>();
  for (int i = 0 ; i < tempstrings.length; i++) {
    tempremoved.add(tempstrings[i]);
  }
  for (int i = 0 ; i < tempstrings.length; i++) {
    if (i == index) {
      tempremoved.remove(i);
      i--;
    }
  }
  tempstrings = new String[tempremoved.size()];
  for (int i = 0 ; i < tempremoved.size(); i++) {
    tempstrings[i] = tempremoved.get(i);
  }
  return tempstrings;
}

int uistarttime = 0;
String berkelyline = "";
String prevberkelyline = "";
public void berkelytalk(String output_) {
  String output = output_;
  //draw berkely
  image(berkely.textures[0], 10, height-height/3, berkely.textures[0].width*8, berkely.textures[0].height*8);
  //draw textbox
  fill(255, 164, 94);
  rect(berkely.textures[0].width*8 + 10, height-height/4, width/2, height/4);
  //draw text
  textSize(width/52);
  fill(0);
  textAlign(LEFT);
  text("Berkely:", berkely.textures[0].width*8 + 40, height-height/4 + 20, width/2 - 30, height/4);
  //create string that elapses charcters
  int numchars = PApplet.parseInt(output.length()*min(1,((float)uistarttime/3)/(float)output.length()));//3 frames per string character
  String finaloutput = "";
  for (int i = 0 ; i < numchars; i++) {
    finaloutput+=output.charAt(i);
  }
  textbox(finaloutput, berkely.textures[0].width*8 + 40);
}
int coins = 0;
boolean ismessagetoplayer = false;//if a textbox is being displayed
String messagetoplayer = "";
public void gotcoin() {
  coins++;
  messagetoplayer = "Berkely coin acheived. Total berkely coins: " + coins;
  ismessagetoplayer = true;
}
int deathamount = 0;
boolean isdying = false;
public void displaydeath() {
  deathamount++;
  fill(0,0,0,255*((float)deathamount/100.0f));//animation takes 100 frames
  rect(0,0,width,height);
  if ((float)deathamount/100.0f >= 1) {
    isdying = false;
    deathamount = 0;
    respawn();
  }
}
public void displaymessage() {//display message to player
  textbox(messagetoplayer, 10+added);
  if (frameCount % (messagetoplayer.length()*3) == messagetoplayer.length()*3-2) {//after 3 frames per character
    messagetoplayer = "";
    ismessagetoplayer = false;
  }
}
public void textbox(String output, float spacing) {
  //draw textbox
  fill(255, 164, 94);
  rect(spacing, height-height/4, width/2, height/4);
  textSize(width/48);
  fill(50);
  textAlign(LEFT);
  text(output, spacing, height-height/4 + 60, width/2 - 30, height/4);
}
public void draw_ui() {
  if (!prevberkelyline.equals(berkelyline)) {//if the line changes since last frame
    uistarttime = 0;
  }
  if (!berkelyline.equals("")) {//if berkely is supposed to talk
    berkelytalk(berkelyline);
    uistarttime++;
  }
  if (ismessagetoplayer) {//if textbox for player should be shown
    displaymessage();
  }
  if (transition) {//if just transitioning into the game
    transamount++;
    float percent = (float)transamount/((float)height/50.0f);//how close to done animation
    fill(158, 129, 79);
    rect(0,height-height*(percent), width, height);
    if (percent >= 2.0f) {//done animation
      transition = false;
      transamount = 0;
    }
  }
  prevberkelyline = berkelyline;
}
String whichmenu = "main";
PImage backdrop;
PImage options;
PImage credits;
PImage[] buttons = new PImage[8];
String[] uipaths = {"story","sandbox","options","credits","mainmenu","mac","laptop","desktop"};
button storybutton;
button sandboxbutton;
button optionsbutton;
button creditsbutton;
button mainmenubutton;
button macbutton;
button laptopbutton;
button desktopbutton;
public void load_menu_assets() {//load menu assets
  backdrop = loadImage("ui/title1.png");
  options = loadImage("ui/optionsmenu.png");
  credits = loadImage("ui/creditsmenu.png");
  for (int i = 0 ; i < buttons.length; i++) {
    buttons[i] = loadImage("ui/"+uipaths[i]+".png");
  }
  storybutton = new button(width/2-256, height/2-10, 0);
  sandboxbutton = new button(width/2-256, height/12*9-10, 1);
  optionsbutton = new button(width-522, height-220, 2);
  creditsbutton = new button(10, height-220, 3);
  mainmenubutton = new button(10, 10, 4);
  macbutton = new button(width/8, (height/7)*3, 5);
  laptopbutton = new button(width/8, (height/7)*4.25f, 6);
  desktopbutton = new button(width/8, (height/7)*5.5f, 7);
}
boolean transition = false;
int transamount = 0;//frames since started transition
public void draw_menu() {//draw the main menu and subcomponents
  if (transition) {//if we are transitioning between menus
    image(backdrop, 0, 0, width, height);
    transamount++;
    float percent = (float)transamount/((float)height/50.0f);//how close to done animation
    if (percent < 1.0f) {//if on starting edge of animation
      fill(158, 129, 79);
      rect(0,height*(1-percent), width, height);
    } else {//if on falling edge
      if (nextisstory) {
        story = true;
      } else if (nextissandbox) {
        ingame = true;
      } else if (nextismenu) {
        whichmenu = "main";
        ingame = false;
        ingamemenu = false;
        story = false;
        nextisstory = false;
        nextissandbox = false;
      }
      drawcurrentmenu();
      fill(158, 129, 79);
      rect(0,height-height*(percent), width, height);
    }
    if (percent >= 2.0f) {//done animation
      transition = false;
      transamount = 0;
      nextismenu = false;
    }
  } else {
    drawcurrentmenu();
  }
}
boolean ingamemenu = false;
public void drawingamemenu() {//draws the options menu while ingame=true
  drawoptionsmenu();
  mainmenubutton.show();
}
public void drawoptionsmenu() {//draw the options menu
  image(options, 0, 0, width, height);
  macbutton.show();
  laptopbutton.show();
  desktopbutton.show();
}
public void drawcurrentmenu() {//when in main menu only
  if (whichmenu == "main") {
    image(backdrop, 0, 0, width, height);
    storybutton.show();
    sandboxbutton.show();
    optionsbutton.show();
    creditsbutton.show();
  } else if(whichmenu == "options") {
    drawoptionsmenu();
  } else if(whichmenu == "credits") {
    image(credits, 0, 0, width, height);
  }
}
class button {//class for clickable menu objects
  PVector pos;
  PImage texture;
  String type;
  boolean hover = false;
  button(float x, float y, int index) {
    pos = new PVector(x,y);
    type = uipaths[index];//which button this is credits options etc..
    texture = buttons[index];
  }
  public void show() {//draw button
    float percent = 10*sin(map(frameCount%200,0,199,0,6.25f));//smooth number to change y by
    update();
    if (hover) tint(200);//if the mouse is over change colour slightly
    image(texture,pos.x,pos.y+percent,texture.width*2,texture.height*2);
    noTint();
  }
  public void update() {//check mouse pressed over button
    if (mouseX > pos.x && mouseY > pos.y && pos.x+texture.width*2 > mouseX && pos.y+texture.height*2 > mouseY) {//if mouse hovering
      hover = true;
      if (lmousedown) activatebutton(type);//if clicked on
    } else {
      hover = false;
    }
  }
}
boolean nextisstory = false;
boolean nextissandbox = false;
boolean nextismenu = false;
//,"mac","laptop","desktop"
public void activatebutton(String type_) {//runs what a button should do, like start the game etc.
  String type = type_;
  if (type == "story") {
    nextisstory = true;
    whichmenu = "main";
  } else if(type == "sandbox") {
    nextissandbox = true;
    whichmenu = "main";
  } else if(type == "options") {
    whichmenu = type;
  } else if(type == "credits") {
    whichmenu = type;
  } else if(type.equals("mainmenu")) {
    whichmenu = "main";
    ingame = false;
    ingamemenu = false;
    story = false;
    nextisstory = false;
    nextissandbox = false;
  } else if(type.equals("mac")) {
    quality = 4;
    maxlights = 50;
    litscreen = createGraphics(PApplet.parseInt(width/quality),PApplet.parseInt(height/quality),P2D);
  } else if(type.equals("laptop")) {
    quality = 3;
    maxlights = 70;
    litscreen = createGraphics(PApplet.parseInt(width/quality),PApplet.parseInt(height/quality),P2D);
  } else if(type.equals("desktop")) {
    quality = 2;
    maxlights = 80;
    litscreen = createGraphics(PApplet.parseInt(width/quality),PApplet.parseInt(height/quality),P2D);
  }
  transition = true;
}

//code from input.pde


public void infoBox(String infoMessage){
  JOptionPane.showMessageDialog(null, infoMessage, "Creator", JOptionPane.INFORMATION_MESSAGE);
}

public String prompt(String s)
{
   String entry = JOptionPane.showInputDialog(s);
   if (entry == null)
      return null;
   return entry;
}

public String getString(String s)
{
   return prompt(s);
}

public int getInt(String s)
{
   return Integer.parseInt(getString(s));
}

public long getLong(String s)
{
   return Long.parseLong(getString(s));
}

public float getFloat(String s)
{
   return Float.parseFloat(getString(s));
}

public double getDouble(String s)
{
   return Double.parseDouble(getString(s));
}

public char getChar(String s)
{
   String entry = prompt(s);
   if (entry.length() >= 1)
      return entry.charAt(0);
   else
      return '\n';
}
  public void settings() {  size(1920,1080,P2D);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "game" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
