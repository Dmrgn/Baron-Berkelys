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
  void show() {
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
  void show_postprocessing() {
    if (!area.equals("none")) {
      JSONObject thisarea = dareas.getJSONObject(area);
      int[] colors = new int[4];
      for (int i = 0 ; i < 4; i++) {
        colors[i] = thisarea.getJSONArray("color").getInt(i);
      }
      postprocessing.fill(colors[0]*max(0.2,time/255),colors[1]*max(0.2,time/255),colors[2]*max(0.2,time/255),colors[3]);
      postprocessing.noStroke();
      postprocessing.ellipse(pos.x*128+64,pos.y*128+64,512,512);
    }
  }
  boolean ispush() {
    if (data.getString("moveable").equals("true")) {
      return true;
    }
    return false;
  }
  boolean pushwall() {
    PVector otherpos = new PVector(drudge.pos.x,drudge.pos.y);
    otherpos.add(new PVector(64,128));
    otherpos.sub(PVector.mult(drudge.vel,5));
    int thisx = int(pos.x);
    int thisy = int(pos.y);
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
  boolean iswall() {
    if (data.getString("wall").equals("true")) {
      return true;
    }
    return false;
  }
  boolean checkcol(PVector point_) {
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
  void show() {
    if (hastexture) {
      animate();
      canvas.noStroke();
      canvas.image(frames[curframe],128*pos.x+offset.x, 128*pos.y+offset.y);
    }
  }
  void showshadow() {
    if (data.getString("blockslight").equals("true")) {
      litscreen.image(frames[curframe],(128*pos.x+offset.x)/quality, (128*pos.y-(frames[curframe].height-128)+offset.y)/quality,frames[curframe].width/quality,frames[curframe].height/quality);
    }
    if (!data.isNull("light") && PVector.dist(PVector.mult(pos,128), drudge.pos) < 512) {//this decoration emits light
      lights.setFloat(lights.size(),map(((128*pos.x+offset.x)*camera_distance+camera_position.x*camera_distance)/width,0,1,0,0.5));
      lights.setFloat(lights.size(),map(1-((128*pos.y+offset.y)*camera_distance+camera_position.y*camera_distance)/height,0,1,0.5,1));
      int[] colors = new int[3];
      for (int i = 0 ; i < colors.length; i++) {
        lights.setFloat(lights.size(),(float)data.getJSONArray("light").getInt(i)/(float)255);
      }
    }
  }
  void show_postprocessing() {
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
  boolean checkcol(PVector point_) {
    PVector point = point_;
    if (hastexture) {
      if (point.x > pos.x*128+offset.x && point.y > pos.y*128+offset.y + frames[0].height/2 && point.x < pos.x*128 +offset.x + frames[0].width && point.y < pos.y*128 +offset.y + frames[0].height) {
        return true;
      }
    }
    return false;
  }
  boolean iscoin() {
    if (!data.isNull("coin")) {
      return true;
    }
    return false;
  }
  boolean iswall() {
    if (data.getString("wall").equals("true")) {
      return true;
    }
    return false;
  }
}

decoration[] sortdecorations(decoration[] decs_) {//sort decorations by draw order
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
  void createframes() {
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
  void animate() {
    if (speed != 0) {
      if (frameCount % speed == 0) {
        curframe++;
        curframe = curframe%dframes.length;
      }
    }
  }
}
