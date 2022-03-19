//Daniel Morgan 2021-06-23
//This tool is not optimized for user experience and is made for testing and creative purposes rather than for inclusion in a final product
//A simplified version of the game that includes no player or player oriented camera.
//meant for creating/editing levels which are stored in data/scenes
//you can load levels by pressing "l" and entering any of the file names located in data/scenes (eg. 'day')
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

void setup() {
  noSmooth();
  size(1820,1024);
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

void draw() {
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
  fill(255, 255, 255, 45);
  rect(int(mousepos.x)*128,int(mousepos.y)*128,128,128);
  _mouseX = mouseX;
  _mouseY = mouseY;
}
