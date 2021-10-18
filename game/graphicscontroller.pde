//does nothing so far because I have not implemented player and entity animation states, particles, graphics settings etc.
void draw_canvas_layer() {
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
void draw_litscreen_layer() {
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
void draw_postprocessing_layer() {
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
void handle_lights() {
  PVector mousepos = new PVector(mouseX/camera_distance, mouseY/camera_distance);
  float x = map(mousepos.x, 0, width, 0, 1), y = map(mousepos.y, 0, height, 1, 0);
  lights.setFloat(lights.size(), -100.0);
  lights.setFloat(lights.size(), 0.0);
  lights.setFloat(lights.size(), 0.0);
  lights.setFloat(lights.size(), 0.0);
  lights.setFloat(lights.size(), 0.0);
  //convert lights json array to float array
  float[] finallights = new float[min(maxlights,lights.size())];
  for (int i = 0 ; i < min(maxlights,lights.size()); i++) {
    finallights[i] = lights.getFloat(i);
  }
  //send uniforms to shader
  fragshader.set("numLights", int(finallights.length/5));
  fragshader.set("reso", sratio);
  fragshader.set("lightscontain", finallights);
}
