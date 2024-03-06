package mealplanner;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class Menu {
    public void runMenu() throws SQLException, IOException {
        int flag = 0;
        final Scanner sc = new Scanner(System.in);
        MealDB mealDB = MealDB.getInstance();
        while(flag == 0) {
            System.out.println("What would you like to do (add, show, plan, save, exit)?");
            String option = sc.nextLine();
            switch (option) {
                case "add":
                    LinkedHashMap<String, String> newMeal = MealBuilder.buildNewMeal();
                    MealDB.addMeal(newMeal);
                    System.out.println("The meal has been added!");
                    break;
                case "show":
                    System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
                    int check = 0;
                    String mealCategory = null;
                    while (check == 0) {
                        mealCategory = sc.nextLine();
                        mealCategory = mealCategory.trim().toLowerCase();
                        if ("breakfast".equals(mealCategory) || "lunch".equals(mealCategory)
                                || "dinner".equals(mealCategory)) {
                            check = 1;
                        } else {
                            System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
                        }
                    }
                    MealDB.printMealsInfo(mealCategory);
                    break;
                case "plan":
                    PlanBuilder.createPlan();
                    MealDB.setPlan(PlanBuilder.getPlan());
                    break;
                case "save":
                    if (MealDB.plan.isEmpty()) {
                        System.out.println("Unable to save. Plan your meals first.");
                        break;
                    }
                    MealDB.createShoppingList();
                    System.out.println("Input a filename:");
                    MealDB.createShoppingListFile(sc.nextLine());
                    System.out.println("Saved!");
                    break;
                case "exit":
                    System.out.println("Bye!");
                    MealDB.closeDatabaseConnection();
                    flag = 1;
                    break;
                default:
                    break;
            }
        }
        sc.close();
    }
}
