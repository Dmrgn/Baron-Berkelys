float _mouseX = mouseX;
float _mouseY = mouseY;
int lastdragged = 0;
void mouseDragged() {
  if (mouseButton == RIGHT) {
    println("Move");
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
void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  camera_distance -= e/10;
  if (camera_distance < 0.1) {
    camera_distance = 0.1;
  }
}
void mouseClicked() {
  if (mouseButton == LEFT) {
    place();
  }
}
void place() {
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
