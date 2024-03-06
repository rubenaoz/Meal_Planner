package mealplanner;
import java.io.IOException;
import java.sql.*;

public class Main {
  public static void main(String[] args) throws SQLException, IOException {
      Menu menu = new Menu();
      menu.runMenu();
  }
}