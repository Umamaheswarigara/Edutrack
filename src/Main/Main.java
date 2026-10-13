package Main;

import java.util.Scanner;

/**
 * Main Entry Point for EduTrack – Intelligent University Academic Platform.
 * Provides an interactive menu system to run individual or all algorithmic modules.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            printHeader();
            printMenu();
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\n[ERROR] Invalid input! Please enter a number between 1 and 8.\n");
                continue;
            }

            System.out.println();
            switch (choice) {
                case 1:
                    M1_StringAlgorithms.runM1();
                    break;
                case 2:
                    M2_SuffixStructures.runM2();
                    break;
                case 3:
                    M3_AdvancedDP.runM3();
                    break;
                case 4:
                    M4_NetworkFlow.runM4();
                    break;
                case 5:
                    M5_NPCompleteness.runM5();
                    break;
                case 6:
                    M6_RandomizedParallel.runM6();
                    break;
                case 7:
                    System.out.println("Executing ALL Modules Sequentially (M1 to M6)...\n");
                    M1_StringAlgorithms.runM1();
                    System.out.println();
                    M2_SuffixStructures.runM2();
                    System.out.println();
                    M3_AdvancedDP.runM3();
                    System.out.println();
                    M4_NetworkFlow.runM4();
                    System.out.println();
                    M5_NPCompleteness.runM5();
                    System.out.println();
                    M6_RandomizedParallel.runM6();
                    System.out.println("\n>>> ALL MODULES EXECUTED SUCCESSFULLY! <<<\n");
                    break;
                case 8:
                    exit = true;
                    System.out.println("Thank you for using EduTrack Platform. Exiting program...");
                    break;
                default:
                    System.out.println("[ERROR] Choice out of range! Please enter a number between 1 and 8.");
            }

            if (!exit) {
                System.out.println("\nPress ENTER to return to the Main Menu...");
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }
        }
        scanner.close();
    }

    private static void printHeader() {
        System.out.println("=============================================");
        System.out.println("      EDUTRACK – UNIVERSITY ACADEMIC         ");
        System.out.println("             MANAGEMENT SYSTEM               ");
        System.out.println("=============================================");
    }

    private static void printMenu() {
        System.out.println("1. M1 - String Algorithms");
        System.out.println("2. M2 - Suffix Structures");
        System.out.println("3. M3 - Advanced Dynamic Programming");
        System.out.println("4. M4 - Network Flow");
        System.out.println("5. M5 - NP-Completeness & Approximation");
        System.out.println("6. M6 - Randomized & Parallel Algorithms");
        System.out.println("7. Run ALL Modules");
        System.out.println("8. Exit");
        System.out.println("---------------------------------------------");
    }
}