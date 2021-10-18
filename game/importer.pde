import java.io.File;

File[] files;

//this file imports levels
//tiles are stored with the decorations as part of them, so tile.decorations is a list of all decorations in tile
void loadscene(String url) {
  JSONObject iloaded = new JSONObject();//imported file
  try {
    iloaded = loadJSONObject(url);
    if (true) {//if loaded correctly
      drudge.pos = new PVector(0,0);
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
          if (idecorations[j].data.getString("name").equals("playerspawn") && drudge.pos.x == 0) {//move player to playerspawn if hasent already moved
            drudge.pos = new PVector(idecorations[j].pos.x*128+idecorations[j].offset.x,idecorations[j].pos.y*128+idecorations[j].offset.y);
          }
        }
        tiles[itile.getInt("x")][itile.getInt("y")].decorations = idecorations;
      }
      JSONArray ienemies = iloaded.getJSONArray("enemies");
      enemies = new enemy[ienemies.size()];
      for (int i = 0 ; i < ienemies.size(); i++) {
        enemies[i] = new enemy(ienemies.getJSONObject(i).getFloat("x"),ienemies.getJSONObject(i).getFloat("y"),ienemies.getJSONObject(i).getString("type"));
      }
    }
    //load meta data like time etc.
    time = iloaded.getInt("time");
  } catch(Exception e) {
    println("Loading failed " + url);
  }
}
