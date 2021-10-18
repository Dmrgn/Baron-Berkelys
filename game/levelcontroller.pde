int step = 0;
int level = 0;
int lls = 0;//last level switch
int lss = 0;//last stage switch
int levelstage = 0;
String objective = "";
void play_story() {
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
    float percent = (float)transamount/((float)height/50.0);//how close to done animation
    fill(158, 129, 79);
    rect(0,height-height*(percent), width, height);
    if (percent >= 2.0) {//done animation
      transition = false;
      transamount = 0;
    }
  }
}
void do_hard_coded() {
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
    messagetoplayer = ""+((6000.0-(float)lss))/60.0;//a minute 30 seconds
    ismessagetoplayer = true;
    added = width-70;
    if(((6000.0-(float)lss))/60.0 <= 0) {ismessagetoplayer = false; nextstage();};};
  if (level == 3 && levelstage == 2) {
    ingame=false;
    nextstep();
    added = 0;};
  //if (level == 1 && levelstage == 4) { if(lss > 60) nextstage();}; // berkely talk
  //if (level == 1 && levelstage == 3) checkentities(1280,384,1664,512,5);//if 5 entities are in the chickenpen
  //if (level == 1 && levelstage == 4) {settile(13,6,"grass");settile(14,6,"grass");settile(12,6,"leaves");settile(15,6,"leaves");nextstage();};//open river passage
}
float added = 0;
void titletext(String title, int delay) {
  background(0);
  textAlign(CENTER,CENTER);
  textSize(70 + min(14,lls/4));
  fill(255);
  text(title,width/2,height/2);
  if (lls == delay || skip) nextstep();
}
void nextstep() {
  skip = false;
  step++;
  lls = 0;
}
void nextstage() {
  levelstage++;
  lss = 0;
}
void die() {
  isdying = true;
}
void respawn() {
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
void startlevel(String path) {
  loadscene("scenes/"+path+".json");
  levelstage = 0;
  level++;
  transition = false;
  transamount = 0;
  ingame = true;
}
void startobjective(String text) {
  titletext("Objective:\n"+text, 250);
  objective = text;
}

void checkentities(float x, float y, float x1, float y1, int amount) {
  int numentites = 0;
  for (int i = 0 ; i < enemies.length; i++) {
    PVector thispos = new PVector(enemies[i].pos.x, enemies[i].pos.y);
    if (thispos.x > x && thispos.x < x1 && thispos.y > y && thispos.y < y1 && enemies[i].dead == false) {//if inbetween invisible box of (x,y) and (x1,y1)
      numentites++;
    }
  }
  if (numentites >= amount) nextstage();
}
int entitiesinportal() {
  int total = 0;
  for (int i = 0 ; i < enemies.length; i++) {
    if (enemies[i].dead) {
      total++;
    }
  }
  return total;
}
String tileat(int x, int y) {
  return tiles[x][y].data.getString("name");
}
void settile(int x, int y, String type) {
  tiles[x][y] = new tile(x, y, type);
}
boolean playerintile(float x, float y) {
  if (drudge.tilepos.x == x && drudge.tilepos.y == y) return true;
  return false;
}
PVector cutamount = new PVector(0,0);
PVector prevdir = new PVector(0,0);
void panto(float x_, float y_, int holdtime) {//pan to position on screen for given time
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
