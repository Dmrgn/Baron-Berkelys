//moves the camera around player and keeps track of player input
boolean cutscene = false;
boolean hold = false; //go straight to cutscene point and hold

void position_camera_canvas() {
  camera_position = new PVector(-drudge.pos.x+(width/2/camera_distance)-64,-drudge.pos.y+(height/2/camera_distance)-64);
  if(cutscene) {
    camera_position.sub(cutamount);
  }
  canvas.translate((int)camera_position.x,(int)camera_position.y);
}
void position_camera_litscreen() {
  camera_position = new PVector(-drudge.pos.x+(width/2/camera_distance)-64,-drudge.pos.y+(height/2/camera_distance)-64);
  if(cutscene) {
    camera_position.sub(cutamount);
  }
  litscreen.translate(int(camera_position.x/quality),int(camera_position.y/quality));
}
void position_camera_postprocessing() {
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
void keyPressed() {
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

void keyReleased() {
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

void mousePressed() {
  if (mouseButton == LEFT) {
    lmousedown = true;
  }
  if (mouseButton == RIGHT) {
    rmousedown = true;
  }
}
void mouseReleased() {
  if (mouseButton == LEFT) {
    lmousedown = false;
  }
  if (mouseButton == RIGHT) {
    rmousedown = false;
  }
}
