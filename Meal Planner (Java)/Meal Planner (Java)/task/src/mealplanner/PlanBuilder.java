package mealplanner;

import java.sql.SQLException;
import java.util.*;

enum Days {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

public class PlanBuilder {
    final static Scanner sc = new Scanner(System.in);
    final static LinkedHashSet<LinkedHashMap<String, String>> plan = new LinkedHashSet<>();

    static void createPlan() throws SQLException {
        MealDB.plan.clear();
        MealDB.planMealsId.clear();
        MealDB.shoppingList.clear();
        for (int i = 0; i < Days.values().length; i ++) {
            LinkedHashMap<String, String> day = new LinkedHashMap<>();
            String currentDay = Days.values()[i].toString().substring(0, 1) +
                    Days.values()[i].toString().substring(1).toLowerCase();
            System.out.println(currentDay);
            // List all breakfasts
            // Choose breakfast, check spelling, add to hashmap
            MealDB.printMealNamesByCategory("breakfast");
            System.out.println("Choose the breakfast for " + currentDay + " from the list above:");
            String breakfast = checkMeal("breakfast");
            // Choose lunch, check meal, add to hashmap
            MealDB.printMealNamesByCategory("lunch");
            System.out.println("Choose the lunch for " + currentDay + " from the list above:");
            String lunch = checkMeal("lunch");
            // Choose dinner, check meal, add to hashmap
            MealDB.printMealNamesByCategory("dinner");
            System.out.println("Choose the dinner for " + currentDay + " from the list above:");
            String dinner = checkMeal("dinner");
            // Add day hashmap to plan
            day.put("day", currentDay);
            day.put("breakfast", breakfast);
            day.put("lunch", lunch);
            day.put("dinner", dinner);
            // Add day to database
            MealDB.addDay(day);
            plan.add(day);
            System.out.println("Yeah! We planned the meals for " + currentDay + ".");
            System.out.println();
        }
        printFullPlan();
    }

    private static void printFullPlan() {
        for (HashMap<String, String> day : plan) {
            System.out.println(day.get("day"));
            System.out.println("Breakfast: " + day.get("breakfast"));
            System.out.println("Lunch: " + day.get("lunch"));
            System.out.println("Dinner: " + day.get("dinner"));
            System.out.println();
        }
    }

    static String checkMeal(String category) throws SQLException {
        boolean check = false;
        String meal = null;
        while (!check) {
            meal = sc.nextLine().trim().toLowerCase();
            if (MealDB.checkMealExists(meal, category)) {
                check = true;
            } else {
                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
            }
        }
        return meal;
    }

    public static LinkedHashSet<LinkedHashMap<String, String>> getPlan() {
        return plan;
    }

}
