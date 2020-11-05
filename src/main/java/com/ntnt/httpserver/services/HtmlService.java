package com.ntnt.httpserver.services;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.ntnt.httpserver.enums.NotiType;
import com.ntnt.httpserver.models.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HtmlService {

    private final String notiGeneralUrl = "http://sv.dut.udn.vn/WebAjax/evLopHP_Load.aspx?E=CTRTBSV";
    private final String notiClassUrl   = "http://sv.dut.udn.vn/WebAjax/evLopHP_Load.aspx?E=CTRTBGV";
    private final String pageParam      = "PAGETB";
    private final String sessionName    = "ASP.NET_SessionId";
    private final String loginUrl       = "http://sv.dut.udn.vn/PageDangNhap.aspx";
    private final String scheduleUrl    = "http://sv.dut.udn.vn/WebAjax/evLopHP_Load.aspx?E=TTKBLoad&Code=";
    private final String scoreUrl       = "http://sv.dut.udn.vn/PageKQRL.aspx";

    public List<NotificationEntity> getNotifications(
            NotiType type,
            int page) {
        try {
            URL targetUrl = null;
            switch (type) {
                case GENERAL:
                    targetUrl = new URL(notiGeneralUrl + String.format("&%s=%d", pageParam, page));
                    break;
                case CLASS:
                    targetUrl = new URL(notiClassUrl + String.format("&%s=%d", pageParam, page));
                    break;
                default:
                    return null;
            }

            Document document = Jsoup.parse(targetUrl, 3000);
            Elements elements = document.getElementsByClass("tbBox");
            List<NotificationEntity> notifications = new ArrayList<>();
            elements.forEach(el -> {
                NotificationEntity notification = new NotificationEntity();
                Element caption = el.getElementsByClass("tbBoxCaption").get(0);

                String date = caption.select("b > span")
                        .first()
                        .text();
                date.substring(0, date.length() - 1);

                String title = caption.select("span")
                        .last()
                        .text();
                String content = el.getElementsByClass("tbBoxContent")
                        .first()
                        .html();

                notification.setType(NotiType.GENERAL);
                notification.setTitle(title);
                notification.setContent(content);

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

                try {
                    notification.setDate(format.parse(date));
                    notifications.add(notification);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
            return notifications;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList getDatasAfterLogin(String username, String password) {
        ArrayList datas = new ArrayList();
        try {
            WebClient webClient = new WebClient();
            webClient.getOptions().setCssEnabled(false);
            webClient.setJavaScriptTimeout(4000);
            webClient.getOptions().setTimeout(10000);

            WebRequest webRequest   = new WebRequest(new URL(loginUrl), HttpMethod.GET);
            HtmlPage page           = webClient.getPage(webRequest);
            HtmlForm loginForm      = (HtmlForm) page.getElementById("ctl00");
            loginForm.getInputByName("ctl00$MainContent$DN_txtAcc").setValueAttribute(username);
            loginForm.getInputByName("ctl00$MainContent$DN_txtPass").setValueAttribute(password);
            HtmlPage userHomePage = loginForm.getInputByName("ctl00$MainContent$QLTH_btnLogin").click();

            // get class from the input tag
            HtmlInput inputClass = (HtmlInput) userHomePage.getElementById("CN_txtLop");
            datas.add(inputClass.getValueAttribute());

            Set<Cookie> cookies = webClient.getCookies(new URL(loginUrl));
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(sessionName)) {
                    datas.add(cookie);
                }
            }

            webClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datas;
    }

    public List<ScheduleEntity> getSchedules(String cookie, String type) {
        List<ScheduleEntity> schedules = new ArrayList<>();
        String[] seperateCookie = cookie.split("=");
        try {
            Document document = Jsoup.connect(scheduleUrl + type)
                    .cookie(seperateCookie[0], seperateCookie[1])
                    .ignoreHttpErrors(true)
                    .get();

            Element table = document.getElementById("TTKB_GridInfo");
            Elements rows = table.getElementsByClass("GridRow");
            rows.remove(rows.last());

            for (Element row : rows) {
                Elements cells = row.children();

                String dataCell3 = cells.get(3).text().replace(" ", "");
                ScheduleEntity schedule = new ScheduleEntity(
                        0,
                        cells.get(1).text(),
                        cells.get(2).text(),
                        Float.parseFloat((dataCell3.isEmpty()) ? "-1" : dataCell3),
                        cells.get(4).attr("class").contains("GridCheck"),
                        cells.get(5).text(),
                        cells.get(6).text(),
                        cells.get(7).text()
                );
                schedules.add(schedule);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return schedules;
    }

    public ScoreResponse getScores(String cookie) {
        List<ScoreEntity> scores = new ArrayList<>();
        List<TotalScoreEntity> totalScores = new ArrayList<>();

        String[] seperateCookie = cookie.split("=");
        try {
            Document document = Jsoup.connect(scoreUrl)
                    .cookie(seperateCookie[0], seperateCookie[1])
                    .ignoreHttpErrors(true)
                    .get();

            Element scoreTable = document.getElementById("KQRLGridKQHT");
            Element totalScoreTable = document.getElementById("KQRLGridTH");

            Elements totalScoreRows = totalScoreTable.getElementsByClass("GridRow");
            Elements scoreRows = scoreTable.getElementsByClass("GridRow");
            scoreRows.remove(scoreRows.last());

            // hanlde cells of scores
            for (Element row : scoreRows) {
                Elements cells = row.children();

                String credit = cells.get(5).text().trim();
                String score1 = cells.get(7).text().trim();
                String score2 = cells.get(8).text().trim();
                String score3 = cells.get(9).text().trim();
                String score4 = cells.get(10).text().trim();
                String score5 = cells.get(11).text().trim();
                String score6 = cells.get(12).text().trim();
                String score7 = cells.get(13).text().trim();
                String score8 = cells.get(14).text().trim();
                ScoreEntity score = new ScoreEntity(
                        0,
                        cells.get(1).text().trim(),
                        cells.get(3).text().trim(),
                        cells.get(4).text().trim(),
                        Float.parseFloat((credit.isEmpty()) ? "-1" : credit),
                        Float.parseFloat((score1.isEmpty()) ? "-1" : score1),
                        Float.parseFloat((score2.isEmpty()) ? "-1" : score2),
                        Float.parseFloat((score3.isEmpty()) ? "-1" : score3),
                        Float.parseFloat((score4.isEmpty()) ? "-1" : score4),
                        Float.parseFloat((score5.isEmpty()) ? "-1" : score5),
                        Float.parseFloat((score6.isEmpty()) ? "-1" : score6),
                        Float.parseFloat((score7.isEmpty()) ? "-1" : score7),
                        Float.parseFloat((score8.isEmpty()) ? "-1" : score8),
                        cells.get(15).text().trim()
                );
                scores.add(score);
            }

            // handle cells of totalScores
            for(Element row : totalScoreRows){
                Elements cells = row.children();

                String totalCredit      = cells.get(1).text().trim();
                String restCredit       = cells.get(2).text().trim();
                String score1           = cells.get(3).text().trim();
                String score2           = cells.get(4).text().trim();
                String score3           = cells.get(5).text().trim();
                String activityScore    = cells.get(7).text().trim();

                TotalScoreEntity totalScore = new TotalScoreEntity(
                        null,
                        cells.get(0).text().trim(),
                        Float.parseFloat((totalCredit.isEmpty()) ? "-1" : totalCredit),
                        Float.parseFloat((restCredit.isEmpty()) ? "-1" : restCredit),
                        Float.parseFloat((score1.isEmpty()) ? "-1" : score1),
                        Float.parseFloat((score2.isEmpty()) ? "-1" : score2),
                        Float.parseFloat((score3.isEmpty()) ? "-1" : score3),
                        cells.get(6).text().trim(),
                        Integer.parseInt(((activityScore.isEmpty()) ? "-1" : activityScore)
                        ));
                totalScores.add(totalScore);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ScoreResponse(scores, totalScores);
    }
}
