package org.example;

import org.example.ui.Interface;

public class Main {
    public static void main(String[] args) {
        Interface ui = new Interface();
        ui.setVisible(args.length == 0);
    }
}