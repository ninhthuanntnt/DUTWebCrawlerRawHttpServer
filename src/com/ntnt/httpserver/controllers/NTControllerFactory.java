package com.ntnt.httpserver.controllers;

public class NTControllerFactory {

    private static MainController mainController;

    public static NTController getController(Class controllerClass){
        if(controllerClass == MainController.class){
            if(mainController == null){
                mainController = new MainController();
            }
            return mainController;
        }
        return null;
    }
}
