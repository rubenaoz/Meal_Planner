package mealplanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.sql.*;

public class MealDB {
    public static LinkedHashSet<LinkedHashMap<String, String>> allMeals = new LinkedHashSet<>();
    public static LinkedHashSet<LinkedHashMap<String, String>> plan = new LinkedHashSet<>();
    public static LinkedHashMap<String, Integer> shoppingList = new LinkedHashMap<>();
    public static List<Integer> planMealsId = new ArrayList<>();
    public final static String DB_URL = "jdbc:postgresql://localhost:5432/meals_db";
    public final static String USER = "postgres";
    public final static String PASS = "1111";
    private final static Scanner sc = new Scanner(System.in);

    private static MealDB instance;

    private static Connection connection;

    private MealDB() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(true);
        createTables();
        loadStoredMeals();
        loadPlan();
        loadMealsById();
    }

    public static MealDB getInstance() throws SQLException {
        try {
            if (instance == null ) instance = new MealDB();
            return instance;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Unable to connect to database.");
        }
    }

    public static void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE SEQUENCE if not exists meals_sequence start 1 increment 1 cache 1");
        statement.executeUpdate("CREATE SEQUENCE if not exists ingredients_sequence start 1 increment 1 cache 1");
        statement.executeUpdate("create table if not exists meals (" +
                "meal_id integer primary key," +
                "category varchar(10)," +
                "meal varchar(50)" +
                ")");
        statement.executeUpdate("create table if not exists ingredients (" +
                "ingredient_id integer primary key," +
                "ingredient varchar(30)," +
                "meal_id integer" +
                ")");
        statement.executeUpdate("create table if not exists plan (" +
                "day varchar(10)," +
                "breakfast varchar(30)," +
                "lunch varchar(30)," +
                "dinner varchar(30)" +
                ")");
        statement.close();
    }

    private static void loadStoredMeals() throws SQLException {
        Statement statement = connection.createStatement();
        Statement statement2 = connection.createStatement();
        ResultSet rs = statement.executeQuery("select * from meals");
        // Read the result set
        while (rs.next()) {
            LinkedHashMap<String, String> storedMeal = new LinkedHashMap<>();
            List<String> storedIngredients = new ArrayList<>();
            int id = rs.getInt("meal_id");
            storedMeal.put("Category", rs.getString("category"));
            storedMeal.put("Name", rs.getString("meal"));
            ResultSet rs2 = statement2.executeQuery("select * from ingredients where meal_id = " + id);
            while (rs2.next()) {
                storedIngredients.add(rs2.getString("ingredient"));
            }
            String storedIngredientsString = String.join(", ", storedIngredients);
            storedMeal.put("Ingredients", storedIngredientsString);
            allMeals.add(storedMeal);
        }
        statement.close();
        statement2.close();
    }

    public static void loadPlan() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM plan");
        while (rs.next()) {
            LinkedHashMap<String, String> day = new LinkedHashMap<>();
            day.put("day", rs.getString("day"));
            day.put("breakfast", rs.getString("breakfast"));
            day.put("lunch", rs.getString("lunch"));
            day.put("dinner", rs.getString("dinner"));
            plan.add(day);
        }
        statement.close();
    }

    public static void loadMealsById() throws SQLException {
        Statement statement = connection.createStatement();
        for (HashMap<String, String> day : plan) {
            for (Map.Entry<String, String> entry : day.entrySet()) {
                if (!Objects.equals(entry.getKey(), "day")) {
                    ResultSet rs = statement.executeQuery("SELECT meal_id FROM meals WHERE " +
                            "meal = '" + entry.getValue() + "' AND category = '" + entry.getKey() + "'");
                    while(rs.next()) {
                        planMealsId.add(rs.getInt("meal_id"));
                    }
                }
            }
        }
        statement.close();
    }

    public static void addMeal(LinkedHashMap<String, String> newMeal) throws SQLException {
        allMeals.add(newMeal);
        String mealCategory = null;
        String mealName = null;
        String[] ingredients = null;
        for (Map.Entry<String, String> entry : newMeal.entrySet()) {
            if (entry.getKey().equals("Category")) {
                mealCategory = entry.getValue();
            }
            if (entry.getKey().equals("Name")) {
                mealName = entry.getValue();
            }
            if (entry.getKey().equals("Ingredients")) {
                ingredients = entry.getValue().split(",");
            }
        }
        String stringSql ="INSERT INTO meals (category, meal, meal_id) " +
                "VALUES  (?, ?, nextval('meals_sequence')) RETURNING meal_id";
        int mealId = 0;
        try (PreparedStatement insertMeal = connection.prepareStatement(stringSql)) {
            insertMeal.setString(1, mealCategory);
            insertMeal.setString(2, mealName);
            ResultSet resultSet = insertMeal.executeQuery();
            if (resultSet.next()) {
                mealId = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Failed to add meal to database.");
        }
        String stringSql2 = "INSERT INTO ingredients (ingredient, meal_id, ingredient_id) " +
                "VALUES (?, ?, nextval('ingredients_sequence'))";
        for (String ing : ingredients) {
            try (PreparedStatement insertIngredients = connection.prepareStatement(stringSql2)) {
                String ingTrimmed = ing.trim();
                insertIngredients.setString(1, ingTrimmed);
                insertIngredients.setInt(2, mealId);
                insertIngredients.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Failed to add ingredients to database.");
            }
        }
    }

    public static void printMealsInfo(String category) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM meals WHERE category = '" + category + "'");
        int count = 0;
        while (rs.next()) {
            count++;
        }
        if (count == 0) {
            System.out.println("No meals found.");
        } else {
            System.out.println("Category: " + category + "\n");
            for (HashMap<String, String> entry : allMeals) {
                if (entry.containsValue(category)) {
                    for (Map.Entry<String, String> meal : entry.entrySet()) {
                        if (meal.getKey().equals("Ingredients")) {
                            System.out.println(meal.getKey() + ":");
                            String[] ingredientsArr = meal.getValue().split(",");
                            for (String ing : ingredientsArr) {
                                String ingTrimmed = ing.trim();
                                System.out.println(ingTrimmed);
                            }
                        } else if (meal.getKey().equals("Name")) {
                            System.out.println(meal.getKey() + ": " + meal.getValue());
                        }
                    }
                    System.out.println();
                }
            }
        }
        statement.close();
    }

    public static void printMealNamesByCategory(String category) throws SQLException {
        Statement statement = connection.createStatement();
        List<String> mealsAlphabetic = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT * FROM meals WHERE category = '" + category + "'");
        int count = 0;
        while (rs.next()) {
            count++;
        }
        if (count == 0) {
            System.out.println("No meals found.");
        } else {
            for (HashMap<String, String> entry : allMeals) {
                if (entry.containsValue(category)) {
                    mealsAlphabetic.add(entry.get("Name"));
                }
            }
        }
        Collections.sort(mealsAlphabetic);
        mealsAlphabetic.forEach(System.out::println);
        statement.close();
    }

    public static void addDay(HashMap<String, String> day) throws SQLException {
        String sqlQuery = "INSERT INTO plan (day, breakfast, lunch, dinner) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, day.get("day"));
            preparedStatement.setString(2, day.get("breakfast"));
            preparedStatement.setString(3, day.get("lunch"));
            preparedStatement.setString(4, day.get("dinner"));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Unable to add day to plan. Please try again.");
        }
    }

    public static void setPlan(LinkedHashSet<LinkedHashMap<String, String>> newPlan) {
        plan = newPlan;
    }

    public static boolean checkMealExists(String mealName, String category) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select meal, meal_id from meals where meal = '" + mealName + "' " +
                "and category = '" + category +"'");
        int check = 0;
        while (rs.next()) {
            check++;
            planMealsId.add(rs.getInt("meal_id"));
        }
        if (check != 0) {
            return true;
        }
        statement.close();
        return false;
    }

    public static void createShoppingList() throws SQLException {
        Statement statement = connection.createStatement();
        for (Integer mealId : planMealsId) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM ingredients WHERE meal_id" +
                    "= " + mealId);
            while (resultSet.next()) {
                String ingredient = resultSet.getString("ingredient");
                if (shoppingList.containsKey(ingredient)) {
                    Integer quantity = shoppingList.get(ingredient) + 1;
                    shoppingList.put(ingredient, quantity);
                } else {
                    shoppingList.put(ingredient, 1);
                }
            }
        }
        statement.close();
    }

    public static void createShoppingListFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.createNewFile()) {
            file.delete();
        };
        for (Map.Entry<String, Integer> entry : shoppingList.entrySet()) {
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(entry.getKey());
                if (entry.getValue() > 1) {
                    writer.write(" x" + entry.getValue());
                }
                writer.write("\n");
            } catch (IOException e) {
                System.out.printf("An exception occurred %s", e.getMessage());
            }
        }
    }

    public static void closeDatabaseConnection() {
        try {
            if (instance != null) {
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (SQLException ignored) { }
    }

}
