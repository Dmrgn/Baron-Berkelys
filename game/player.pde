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
  void createframes() {
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
  void show() {
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
  void showshadow() {//draw purple light
    if (lmousedown || rmousedown) {
      lights.setFloat(lights.size(),map(((pos.x+32+camera_position.x)*camera_distance)/width,0,1,0,0.5));
      lights.setFloat(lights.size(),map(1-((pos.y+12+camera_position.y)*camera_distance)/height,0,1,0.5,1));
      lights.setFloat(lights.size(),(float)30/(float)255);
      lights.setFloat(lights.size(),(float)5/(float)255);
      lights.setFloat(lights.size(),(float)100/(float)255);
    }
  }
  void show_postprocessing() {
    if (rmousedown || lmousedown) {
      PVector shift = new PVector((float)frames[0][0].width,(float)frames[0][0].height);//draw fading ellipse around portal
      for (int i = 0 ; i < 100; i+=2) {
        postprocessing.fill(0,0,0,100-i);
        postprocessing.ellipse(pos.x+shift.x/2,pos.y+shift.y/2,shift.x+i,shift.y+i);
      }
    }
  }
  void update() {
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
  void show() {
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
  void update() {
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
        deathamount = floor(deathamount * 0.8);
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
      vel.mult(0.8);
      if (vel.mag() < 0.4) {
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
  void revive() {
    dead = false;
  }
  void death() {
    deathamount++;
    deathamountchange = true;
  }
  void randompath() {//chickencorns and knomes
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
  void charge() {//unicorns
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
  float acceleration = 0.25;
  player(float x, float y) {
    String[] playertypes = {"right","left","idle","righttrans","lefttrans"};
    animation_types = playertypes;
    animation_machine = "idle";
    pos = new PVector(x,y);
    vel = new PVector(0,0);
    data = loadJSONObject("player.json");//player json data/player.json
    createframes();
  }
  void show() {
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
  void showshadow() {
    int index = 0;
    for (int i = 0 ; i < animation_types.length; i++) {
      if (animation_types[i] == animation_machine) {
        index = i;
      }
    }
    litscreen.image(frames[index][curframe],pos.x/quality, pos.y/quality-(frames[index][curframe].height/quality-128/quality),frames[index][curframe].width/quality,frames[index][curframe].height/quality);
  }
  void update() {
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
      if (vel.mag() < 1.5 && movedir.x == 0 && movedir.y == 0) {
        animation_machine = "idle";
      }
      movedir.setMag(acceleration);
      vel.add(movedir);
      vel.limit(maxspeed);
      pos.add(vel);
    }
    if (movedir.x == 0 && movedir.y == 0) {//if not pressing move buttons lose 10% of speed
      vel.mult(0.9);
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
