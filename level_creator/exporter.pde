void savecurrent() {
  JSONObject data = new JSONObject();
  JSONArray extiles = new JSONArray();
  for (int i = 0  ; i < tiles.length; i++) {
    for (int j = 0 ; j < tiles[i].length; j++) {
      JSONObject extile = new JSONObject();
      extile.setString("type", tiles[i][j].data.getString("name"));
      extile.setFloat("x", tiles[i][j].pos.x);
      extile.setFloat("y", tiles[i][j].pos.y);
      JSONArray exdecorations = new JSONArray();
      for (int k = 0 ; k < tiles[i][j].decorations.length; k++) {
        JSONObject exdecoration = new JSONObject();
        exdecoration.setFloat("x",tiles[i][j].decorations[k].offset.x);
        exdecoration.setFloat("y",tiles[i][j].decorations[k].offset.y);
        exdecoration.setString("type",tiles[i][j].decorations[k].data.getString("name"));
        exdecorations.setJSONObject(k, exdecoration);
      }
      extile.setJSONArray("decorations", exdecorations);
      extile.setString("area", tiles[i][j].area);
      extiles.setJSONObject(extiles.size(), extile);
    }
  }
  JSONArray exenemies = new JSONArray();
  for (int i = 0 ; i < enemies.length; i++) {
    JSONObject exenemy = new JSONObject();
    exenemy.setString("type",enemies[i].data.getString("name"));
    exenemy.setString("x",""+enemies[i].pos.x);
    exenemy.setString("y",""+enemies[i].pos.y);
    exenemies.setJSONObject(exenemies.size(),exenemy);
  }
  data.setJSONArray("enemies",exenemies);
  data.setJSONArray("tiles",extiles);
  data.setInt("time",200);//auto set time to day
  String filename = getString("What would you like to name your file?");
  saveJSONObject(data, "data/scenes/"+filename+".json");
}
