package mealplanner;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class MealBuilder {
    private static final Scanner sc = new Scanner(System.in);

    public static LinkedHashMap<String, String> buildNewMeal() {
        LinkedHashMap<String, String> newMeal = new LinkedHashMap<>();
        newMealCategory(newMeal);
        newMealName(newMeal);
        newMealIngredients(newMeal);
        return newMeal;
    }

    public static void newMealCategory(LinkedHashMap<String, String> newMeal) {
        int flag = 0;
        String category = null;
        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?\t");
        while (flag == 0) {
            category = sc.nextLine().trim().toLowerCase();
            if ("breakfast".equals(category) || "lunch".equals(category) || "dinner".equals(category)) {
                flag = 1;
            } else {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }
            if (checkAlphabet(category)) {
                flag = 0;
            }
        }
        newMeal.put("Category", category);
    }

    public static void newMealName(LinkedHashMap<String, String> newMeal) {
        int flag = 0;
        String mealName = null;
        System.out.println("Input the meal's name:\t");
        while (flag == 0) {
            mealName = sc.nextLine().trim().toLowerCase();
            String mealNameWithoutSpaces = mealName.replace(" ", "");
            flag = 1;
            if (checkAlphabet(mealNameWithoutSpaces)) {
                flag = 0;
            }
        }
        newMeal.put("Name", mealName);
    }

    public static void newMealIngredients(LinkedHashMap<String, String> newMeal) {
        int flag = 0;
        String ingredients = null;
        System.out.println("Input the ingredients:\t");
        while (flag == 0) {
            ingredients = sc.nextLine().trim().toLowerCase();
            String[] ingredientsArr = ingredients.split(",", -1);
            int checkStrings = 0;
            for(String ing : ingredientsArr) {
                String ingWithoutSpace = ing.replace(" ","");
                if (checkAlphabet(ingWithoutSpace)) {
                    checkStrings++;
                }
            }
            if (checkStrings == 0) {
                flag = 1;
            }
        }
        newMeal.put("Ingredients", ingredients);
    }

    public static boolean checkAlphabet(String input) {
        if (input.isBlank() || input.isEmpty() ||
                !input.chars().allMatch(Character::isAlphabetic)) {
            System.out.println("Wrong format. Use letters only!");
            return true;
        }
        return false;
    }
}
