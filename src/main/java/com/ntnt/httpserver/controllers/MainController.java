package com.ntnt.httpserver.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.ntnt.httpserver.annotations.RequestMapping;
import com.ntnt.httpserver.enums.NotiType;
import com.ntnt.httpserver.models.*;
import com.ntnt.httpserver.servers.HttpRequest;
import com.ntnt.httpserver.servers.HttpResponse;
import com.ntnt.httpserver.services.HtmlService;
import com.ntnt.httpserver.services.JwtService;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@RequestMapping(value = "/api")
public class MainController implements NTController {

    private HtmlService htmlService = new HtmlService();
    private JwtService jwtService = new JwtService();

    @RequestMapping(value = "/notifications")
    public void getNotification(HttpRequest request, HttpResponse response) {
        String notiType = request.getParameter("notiType");
        String pageNumber = request.getParameter("pageNumber");

        List<NotificationEntity> notificationEntity = htmlService.getNotifications(
                NotiType.valueOf(notiType.toUpperCase()),
                Integer.parseInt(pageNumber)
        );

        String body = "";
        try {
            body = new ObjectMapper().writeValueAsString(notificationEntity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        response.setBody(body);
    }

    @RequestMapping(value = "/login", method = "POST")
    public void doLogin(HttpRequest request, HttpResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        ArrayList datas = htmlService.getDatasAfterLogin(username, password);
        Cookie cookie = (Cookie) datas.get(1);

        if (cookie != null) {
            JwtResponse jwtResponse = new JwtResponse(
                    jwtService.generateToken(
                            cookie.getName() + '=' + cookie.getValue(),
                            username
                    ), (String) datas.get(0)
            );
            response.setBody(new JSONObject(jwtResponse).toString());
        }
    }

    @RequestMapping(value = "/score")
    public void doGetScore(HttpRequest request, HttpResponse response) {
        String token = getBearerToken(request);
        if (token != null && isAuthenticated(token)) {

            UserEntity user = jwtService.getUserFromToken(token);
            ScoreResponse scoreResponse = htmlService.getScores(user.getCrawledWebCookie());

            String body = "";
            try {
                body = new ObjectMapper().writeValueAsString(scoreResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            response.setBody(body);

        }else{
            response.setStatus(403);
        }
    }

    @RequestMapping(value = "/schedule")
    public void doGetShedule(HttpRequest request, HttpResponse response){
        String token = getBearerToken(request);
        if (token != null && isAuthenticated(token)) {

            UserEntity user = jwtService.getUserFromToken(token);
            String type = request.getParameter("type");
            List<ScheduleEntity> schedules = htmlService.getSchedules(user.getCrawledWebCookie(), type);
            String body = "";
            try {
                body = new ObjectMapper().writeValueAsString(schedules);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            response.setBody(body);

        }else{
            response.setStatus(403);
        }
    }

    private String getBearerToken(HttpRequest request) {
        String authorization = request.getHeaders("Authorization");
        if (authorization != null && !authorization.isEmpty()) {
            int indexOfPrefix = authorization.indexOf("Bearer ");
            if (indexOfPrefix >= 0) {
                return authorization.substring(indexOfPrefix + 7);
            }
        }
        return null;
    }

    private boolean isAuthenticated(String token) {
        if (jwtService.isValidatedToken(token)) {
            return true;
        }
        return false;
    }

}
