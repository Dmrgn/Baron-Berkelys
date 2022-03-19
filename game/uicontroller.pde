//misc input/output and ui

boolean grid = false;//grid for debuging

String removecharat(int index, String input) {
  String newstring = "";
  for (int i = 0 ; i < input.length(); i++) {
    if (i != index) {
      newstring += input.charAt(i);
    }
  }
  return newstring;
}

String[] removestringfrom(String[] tempstrings_, int index) {
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
void berkelytalk(String output_) {
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
  int numchars = int(output.length()*min(1,((float)uistarttime/3)/(float)output.length()));//3 frames per string character
  String finaloutput = "";
  for (int i = 0 ; i < numchars; i++) {
    finaloutput+=output.charAt(i);
  }
  textbox(finaloutput, berkely.textures[0].width*8 + 40);
}
int coins = 0;
boolean ismessagetoplayer = false;//if a textbox is being displayed
String messagetoplayer = "";
void gotcoin() {
  coins++;
  messagetoplayer = "Berkely coin acheived. Total berkely coins: " + coins;
  ismessagetoplayer = true;
}
int deathamount = 0;
boolean isdying = false;
void displaydeath() {
  deathamount++;
  fill(0,0,0,255*((float)deathamount/100.0));//animation takes 100 frames
  rect(0,0,width,height);
  if ((float)deathamount/100.0 >= 1) {
    isdying = false;
    deathamount = 0;
    respawn();
  }
}
void displaymessage() {//display message to player
  textbox(messagetoplayer, 10+added);
  if (frameCount % (messagetoplayer.length()*3) == messagetoplayer.length()*3-2) {//after 3 frames per character
    messagetoplayer = "";
    ismessagetoplayer = false;
  }
}
void textbox(String output, float spacing) {
  //draw textbox
  fill(255, 164, 94);
  rect(spacing, height-height/4, width/2, height/4);
  textSize(width/48);
  fill(50);
  textAlign(LEFT);
  text(output, spacing, height-height/4 + 60, width/2 - 30, height/4);
}
void draw_ui() {
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
    float percent = (float)transamount/((float)height/50.0);//how close to done animation
    fill(158, 129, 79);
    rect(0,height-height*(percent), width, height);
    if (percent >= 2.0) {//done animation
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
void load_menu_assets() {//load menu assets
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
  laptopbutton = new button(width/8, (height/7)*4.25, 6);
  desktopbutton = new button(width/8, (height/7)*5.5, 7);
}
boolean transition = false;
int transamount = 0;//frames since started transition
void draw_menu() {//draw the main menu and subcomponents
  if (transition) {//if we are transitioning between menus
    image(backdrop, 0, 0, width, height);
    transamount++;
    float percent = (float)transamount/((float)height/50.0);//how close to done animation
    if (percent < 1.0) {//if on starting edge of animation
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
    if (percent >= 2.0) {//done animation
      transition = false;
      transamount = 0;
      nextismenu = false;
    }
  } else {
    drawcurrentmenu();
  }
}
boolean ingamemenu = false;
void drawingamemenu() {//draws the options menu while ingame=true
  drawoptionsmenu();
  mainmenubutton.show();
}
void drawoptionsmenu() {//draw the options menu
  image(options, 0, 0, width, height);
  macbutton.show();
  laptopbutton.show();
  desktopbutton.show();
}
void drawcurrentmenu() {//when in main menu only
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
  void show() {//draw button
    float percent = 10*sin(map(frameCount%200,0,199,0,6.25));//smooth number to change y by
    update();
    if (hover) tint(200);//if the mouse is over change colour slightly
    image(texture,pos.x,pos.y+percent,texture.width*2,texture.height*2);
    noTint();
  }
  void update() {//check mouse pressed over button
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
void activatebutton(String type_) {//runs what a button should do, like start the game etc.
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
    litscreen = createGraphics(int(width/quality),int(height/quality),P2D);
  } else if(type.equals("laptop")) {
    quality = 3;
    maxlights = 70;
    litscreen = createGraphics(int(width/quality),int(height/quality),P2D);
  } else if(type.equals("desktop")) {
    quality = 2;
    maxlights = 80;
    litscreen = createGraphics(int(width/quality),int(height/quality),P2D);
  }
  transition = true;
}

//code from input.pde
import javax.swing.*;

void infoBox(String infoMessage){
  JOptionPane.showMessageDialog(null, infoMessage, "Creator", JOptionPane.INFORMATION_MESSAGE);
}

String prompt(String s)
{
   String entry = JOptionPane.showInputDialog(s);
   if (entry == null)
      return null;
   return entry;
}

String getString(String s)
{
   return prompt(s);
}

int getInt(String s)
{
   return Integer.parseInt(getString(s));
}

long getLong(String s)
{
   return Long.parseLong(getString(s));
}

float getFloat(String s)
{
   return Float.parseFloat(getString(s));
}

double getDouble(String s)
{
   return Double.parseDouble(getString(s));
}

char getChar(String s)
{
   String entry = prompt(s);
   if (entry.length() >= 1)
      return entry.charAt(0);
   else
      return '\n';
}
