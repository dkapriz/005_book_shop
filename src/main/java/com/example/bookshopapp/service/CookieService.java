package com.example.bookshopapp.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class CookieService {

    public String[] getCookieValuesByContents(String contents) {
        contents = contents.startsWith("/") ? contents.substring(1) : contents;
        contents = contents.endsWith("/") ? contents.substring(0, contents.length() - 1) : contents;
        return contents.split("/");
    }

    public void addValueToCookieResponse(HttpServletResponse response, String contents,
                                         String contentsName, String contentAddValue) {
        addValueToCookieResponse(response, contents, contentsName, Collections.singletonList(contentAddValue));
    }

    public void addValueToCookieResponse(HttpServletResponse response, String contents,
                                         String contentsName, List<String> contentsAddValue) {
        if (contents == null || contents.isEmpty()) {
            Cookie cookie = new Cookie(contentsName, String.join("/", contentsAddValue));
            cookie.setPath("/");
            response.addCookie(cookie);
        } else {
            StringJoiner stringJoiner = new StringJoiner("/");
            stringJoiner.add(contents);
            for (String value : contentsAddValue) {
                if (!contents.contains(value)) {
                    stringJoiner.add(value);
                }
            }
            Cookie cookie = new Cookie(contentsName, stringJoiner.toString());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    public void deleteValueFromCookieBooksResponse(HttpServletResponse response, String contents,
                                                   String contentsName, List<String> contentsRemoveValue) {
        if (contents != null && !contents.isEmpty()) {
            ArrayList<String> cookieBooks = new ArrayList<>(Arrays.asList(contents.split("/")));
            for (String value : contentsRemoveValue) {
                cookieBooks.remove(value);
            }
            Cookie cookie = new Cookie(contentsName, String.join("/", cookieBooks));
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    public static void deleteCookieByName(HttpServletRequest request, String name) {
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                cookie.setMaxAge(0);
            }
        }
    }
}
