package com.library.util;

import java.util.Scanner;

/**
 * Utility class for formatted console I/O.
 * Keeps all print/formatting logic in one place.
 */
public class ConsoleHelper {

    public static final String RESET  = "\u001B[0m";
    public static final String BOLD   = "\u001B[1m";
    public static final String GREEN  = "\u001B[32m";
    public static final String RED    = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN   = "\u001B[36m";
    public static final String BLUE   = "\u001B[34m";

    private static final Scanner scanner = new Scanner(System.in);

    public static void printHeader(String title) {
        String line = "═".repeat(60);
        System.out.println(CYAN + line + RESET);
        System.out.printf(CYAN + "  %-56s  " + RESET + "%n", title);
        System.out.println(CYAN + line + RESET);
    }

    public static void printSubHeader(String title) {
        System.out.println(BLUE + "── " + title + " " + "─".repeat(Math.max(0, 54 - title.length())) + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "✔ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "✘ " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "ℹ " + message + RESET);
    }

    public static void printDivider() {
        System.out.println("─".repeat(60));
    }

    public static String readLine(String prompt) {
        System.out.print(BOLD + prompt + RESET);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(BOLD + prompt + RESET);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                printError("Please enter a valid number.");
            }
        }
    }

    public static int readIntRange(String prompt, int min, int max) {
        while (true) {
            int val = readInt(prompt);
            if (val >= min && val <= max) return val;
            printError("Please enter a number between " + min + " and " + max + ".");
        }
    }

    public static boolean confirm(String prompt) {
        String input = readLine(prompt + " (y/n): ").toLowerCase();
        return input.equals("y") || input.equals("yes");
    }

    public static void pause() {
        readLine("\nPress Enter to continue...");
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Table printing helpers
    public static void printTableRow(String... cols) {
        StringBuilder sb = new StringBuilder("│");
        for (String col : cols) sb.append(String.format(" %-20s│", col));
        System.out.println(sb);
    }

    public static void printTableHeader(String... headers) {
        int width = headers.length * 22 + 1;
        System.out.println("┌" + "─".repeat(width - 2) + "┐");
        StringBuilder sb = new StringBuilder("│");
        for (String h : headers) sb.append(String.format(BOLD + " %-20s" + RESET + "│", h));
        System.out.println(sb);
        System.out.println("├" + "─".repeat(width - 2) + "┤");
    }

    public static void printTableFooter(int colCount) {
        System.out.println("└" + "─".repeat(colCount * 22 - 1) + "┘");
    }
}
