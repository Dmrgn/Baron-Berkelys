import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import javax.swing.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class level_creator extends PApplet {

//a simplified version of the game that includes no player or player oriented camera.
//meant for creating/editing levels which are stored in data/scenes
//you can load levels by pressing "l" and entering "day" or "night" (day and night are two test levels I made)
//you can place tiles and decorations by left clicking
//you can select different objects by pressing "enter" and then entering an object name
//find a list of objects in data/tiles/manifest.json and data/decorations/manifest.json
//you can save levels to data/scenes by pressing "s" and then entering a level name

//CONTROLS
//right click+drag to move camera
//scroll wheel to zoom
//"g" to enable/disable grid
//"s" save level
//"l" load level

PVector camera_position = new PVector(0,0);
float camera_distance = 1;
String layer = "base";
String[] layers = {"base","decorations","areas","enemies"};
boolean grid = true;

JSONObject dtiles;
JSONArray tilemanifest;

JSONObject dareas;
JSONArray areamanifest;

JSONObject denemys;
JSONArray enemymanifest;

enemy[] enemies = new enemy[0];
tile[][] tiles;
PImage[] tileimages = new PImage[0]; //a list of previously loaded tile images
String[] dtileimages = new String[0]; //manifest for tileimages

JSONObject ddecorations;
JSONArray decorationmanifest;
PImage[] decorationimages = new PImage[0]; //a list of previously loaded decoration images
String[] ddecorationimages = new String[0]; //manifest for decorationimages

JSONArray[] allmanifest = new JSONArray[4];

public void setup() {
  
  
  surface.setResizable(true);

  //load tile json
  load_json();

  tiles = new tile[30][30];
  for (int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      tiles[i][j] = new tile(i, j, "empty");
    }
  }
}

public void draw() {
  background(200);
  translate(camera_position.x,camera_position.y);
  scale(camera_distance);
  for(int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      tiles[j][i].show();
    }
  }
  for(int i = 0 ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      for (int k = 0 ; k < tiles[j][i].decorations.length; k++) {
        tiles[j][i].decorations[k].show();
      }
    }
  }
  for (int i = 0 ; i < enemies.length; i++) {
    enemies[i].show();
  }
  if (grid) {
    for (int i = 0 ; i < tiles.length; i++) {
      for (int j = 0 ; j < tiles[i].length; j++) {
        noFill();
        strokeWeight(1);
        stroke(0);
        rect(i*128,j*128,128,128);
      }
    }
  }
  PVector mousepos = new PVector(abs(((mouseX-camera_position.x)/128)/camera_distance)%tiles.length, abs(((mouseY-camera_position.y)/128)/camera_distance)%tiles.length);
  println(floor(mousepos.x) + " " + floor(mousepos.y));
  fill(255, 255, 255, 45);
  rect(PApplet.parseInt(mousepos.x)*128,PApplet.parseInt(mousepos.y)*128,128,128);
  _mouseX = mouseX;
  _mouseY = mouseY;
}
float _mouseX = mouseX;
float _mouseY = mouseY;
int lastdragged = 0;
public void mouseDragged() {
  if (mouseButton == RIGHT) {
    PVector mouse = new PVector(mouseX, mouseY);
    PVector _mouse = new PVector(_mouseX, _mouseY);
    PVector dir = PVector.sub(mouse, _mouse);
    dir.div(4);
    camera_position.add(dir);
  }
  if (lastdragged == 0) {
    if (mouseButton == LEFT) {
      lastdragged = 30;
      place();
    }
    } else {
      lastdragged--;
    }
  }
  public void mouseWheel(MouseEvent event) {
    float e = event.getCount();
    camera_distance -= e/10;
    if (camera_distance < 0.1f) {
      camera_distance = 0.1f;
    }
  }
  public void mouseClicked() {
    if (mouseButton == LEFT) {
      place();
    }
  }
  public void place() {
    PVector clickpos = new PVector(abs(((mouseX-camera_position.x)/128)/camera_distance)%tiles.length, abs(((mouseY-camera_position.y)/128)/camera_distance)%tiles.length);//tiles layer
    PVector clickposnoround = new PVector(abs(((mouseX-camera_position.x)/128)/camera_distance)%tiles.length, abs(((mouseY-camera_position.y)/128)/camera_distance)%tiles.length);//tiles layer
    PVector secondclickpos = new PVector((clickpos.x-(int)clickpos.x)*128, (clickpos.y-(int)clickpos.y)*128);
    if (clickpos.x < tiles.length && clickpos.y < tiles[0].length) {
      if (layer.equals("base")) {
        tiles[(int)clickpos.x][(int)clickpos.y] = new tile((int)clickpos.x, (int)clickpos.y, selected);
      }
      if (layer.equals("decorations")) {
        tiles[(int)clickpos.x][(int)clickpos.y].newdecoration(secondclickpos.x, secondclickpos.y, selected);
      }
      if (layer.equals("areas")) {
        tiles[(int)clickpos.x][(int)clickpos.y].changearea(selected);
      }
      if (layer.equals("enemies")) {
        enemy[] tempenemies = enemies;
        enemies = new enemy[tempenemies.length+1];
        for (int i = 0 ; i < tempenemies.length; i++) {
          enemies[i] = tempenemies[i];
        }
        enemies[enemies.length-1] = new enemy((int)clickpos.x*128+secondclickpos.x,(int)clickpos.y*128+secondclickpos.y,selected);
      }
    }
  }
public void savecurrent() {
  JSONObject data = new JSONObject();
  JSONArray extiles = new JSONArray();
  for (int i = 0  ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      JSONObject extile = new JSONObject();
      extile.setString("type", tiles[i][j].data.getString("name"));
      extile.setFloat("x", tiles[i][j].pos.x);
      extile.setFloat("y", tiles[i][j].pos.y);
      JSONArray exdecorations = new JSONArray();
      for (int k = 0 ; k < tiles[i][j].decorations.length; k++) {
        JSONObject exdecoration = new JSONObject();
        exdecoration.setFloat("x",tiles[i][j].decorations[k].offset.x);
        exdecoration.setFloat("y",tiles[i][j].decorations[k].offset.y);
        exdecoration.setString("type",tiles[i][j].decorations[k].data.getString("name"));
        exdecorations.setJSONObject(k, exdecoration);
      }
      extile.setJSONArray("decorations", exdecorations);
      extile.setString("area", tiles[i][j].area);
      extiles.setJSONObject(extiles.size(), extile);
    }
  }
  JSONArray exenemies = new JSONArray();
  for (int i = 0 ; i < enemies.length; i++) {
    JSONObject exenemy = new JSONObject();
    exenemy.setString("type",enemies[i].data.getString("name"));
    exenemy.setString("x",""+enemies[i].pos.x);
    exenemy.setString("y",""+enemies[i].pos.y);
    exenemies.setJSONObject(exenemies.size(),exenemy);
  }
  data.setJSONArray("enemies",exenemies);
  data.setJSONArray("tiles",extiles);
  data.setInt("time",200);//auto set time to day
  String filename = getString("What would you like to name your file?");
  saveJSONObject(data, "data/scenes/"+filename+".json");
}

//variable prefix 'i' stands for import
//variable prefix 'ex' stands for export
//variable prefix 'd' stands for data
//variable prefix 'j' stands for json
//variable prefix 'temp' stands for temporary
//tiles are stored with the decorations as part of them, so tile.decorations is a list of all decorations in tile
boolean loadingfile = false;
public void loadscene(String url) {
  JSONObject iloaded = new JSONObject();//imported file
  try {
    iloaded = loadJSONObject(url);
  } catch(Exception e) {
    infoBox(e.getMessage());
  }
  if (true) {//if loaded correctly
    loadingfile = true;
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
      }
      tiles[itile.getInt("x")][itile.getInt("y")].decorations = idecorations;
    }
    JSONArray ienemies = iloaded.getJSONArray("enemies");
    enemies = new enemy[ienemies.size()];
    for (int i = 0 ; i < ienemies.size(); i++) {
      enemies[i] = new enemy(ienemies.getJSONObject(i).getFloat("x"),ienemies.getJSONObject(i).getFloat("y"),ienemies.getJSONObject(i).getString("type"));
    }
    loadingfile = false;
  }
}


public void infoBox(String infoMessage){
  JOptionPane.showMessageDialog(null, infoMessage, "Creator", JOptionPane.INFORMATION_MESSAGE);
}

public String prompt(String s)
{
   println(s);
   String entry = JOptionPane.showInputDialog(s);
   if (entry == null)
      return null;
   println(entry);
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
public void load_json() {
  //load all tile json
  tilemanifest = loadJSONArray("tiles/manifest.json");
  println("Loaded tiles tilemanifest");
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
    println(decorationmanifest.getString(i));
    JSONObject ddecoration = loadJSONObject("decorations/"+decorationmanifest.getString(i)+".json");
    println("\t-Loaded " + decorationmanifest.getString(i) + ".json");
    ddecorations.setJSONObject(decorationmanifest.getString(i),ddecoration);
  }
  //load all area json
  areamanifest = loadJSONArray("areas/manifest.json");
  println("Loaded areas areamanifest");
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
  public void changearea(String type_) {
    area = type_;
  }
  public void show() {
    if (hastexture) {
      image(frames[curframe],128*pos.x, 128*pos.y-(frames[curframe].height-128));
      int[] colors = new int[4];
      for (int i = 0 ; i < 4; i++) {
        colors[i] = dareas.getJSONObject(area).getJSONArray("color").getInt(i);
      }
      if (show_areas) {
        noStroke();
        fill(colors[0],colors[1],colors[2],colors[3]);
        rect(128*pos.x, 128*pos.y,128,128);
      }
    }
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
    if (hastexture && !loadingfile) {
      offset.sub(new PVector(frames[0].width/2,frames[0].height/2));//center decoration
    }
  }
  public void show() {
    if (hastexture) {
      animate();
      image(frames[curframe],128*pos.x+offset.x, 128*pos.y+offset.y);
    }
  }
}

class entity {
  PVector pos;
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
          println("Loading textures for player");
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

class enemy extends entity {
  enemy(float x, float y, String type) {
    pos = new PVector(x,y);
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
    if (frameCount % speed == 0) {
      curframe++;
    }
    curframe = curframe%data.getJSONArray("idle").size();
    noStroke();
    int index = 0;
    for (int i = 0 ; i < animation_types.length; i++) {
      if (animation_types[i] == animation_machine) {
        index = i;
      }
    }
    image(frames[index][curframe],pos.x, pos.y);
  }
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
String selected = "grass";
boolean show_areas = true;
public void searchtile(String _object) {
  String object = _object;
  boolean hasfound = false;
  for (int i = 0 ; i < layers.length; i++) {
    for (int j = 0 ; j < allmanifest[i].size(); j++) {
      boolean contains = false;
      if (allmanifest[i].getString(j).equals(object)) {//if the object is found
        layer = layers[i];
        selected = object;
        hasfound = true;
      }
    }
  }
  if (!hasfound) {
    String close = "";
    for (int i = 0 ; i < layers.length; i++) {
      for (int j = 0 ; j < allmanifest[i].size(); j++) {
        String current = allmanifest[i].getString(j);
        String currenttest = current;
        String objecttest = object;
        int commonchars = 0;
        for (int k = 0 ; k < object.length(); k++) {
          if (PApplet.parseInt(currenttest.indexOf(str(object.charAt(k)))) != -1) {
            currenttest = removecharat(PApplet.parseInt(currenttest.indexOf(str(object.charAt(k)))), currenttest);
            //k--;
            commonchars++;
          }
        }
        float similarity = (float)commonchars/(float)objecttest.length();
        if (similarity >= 0.75f) {
          close += current + ", ";
        }
      }
    }
    infoBox("We couldnt find your search. Did you mean: \n" + close);
  }
}
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
    println(tempstrings[i]);
  }
  return tempstrings;
}
public void keyPressed() {
  if (key == 'g') {
    println(key);
    grid = !grid;
  }
  if (key == 's') {
    savecurrent();
  }
  if (key == 'k') {
    show_areas = !show_areas;
  }
  if (key == 'l') {
    String path = getString("What is the file called? (don't include .json)");
    loadscene("scenes/"+path+".json");
  }
  if (keyCode == ENTER) {
    String response = getString("Search for object:");
    searchtile(response);
  }
}
  public void settings() {  size(1820,1024);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "level_creator" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
