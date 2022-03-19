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
  void newdecoration(float x, float y, String type) {
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
  void changearea(String type_) {
    area = type_;
  }
  void show() {
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
  void show() {
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
  void show() {
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
  void createframes() {
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
  void animate() {
    if (speed != 0) {
      if (frameCount % speed == 0) {
        curframe++;
        curframe = curframe%dframes.length;
      }
    }
  }
}
