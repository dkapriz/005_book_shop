package com.example.bookshopapp.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MainMainMenuPagesSeleniumTest {

    private static ChromeDriver driver;

    @BeforeAll
    static void setUp() {
        System.setProperty("webdriver.chrome.driver", "chromedriver/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
    }

    @AfterAll
    static void tearDown() {
        driver.quit();
    }

    @Test
    void testMainMenuPages() throws InterruptedException {
        MainMenuPages mainMenuPages = new MainMenuPages(driver);
        mainMenuPages
                .callPage()
                .pause()
                .callFirstBook()
                .pause()
                .callGenrePage()
                .pause()
                .callFantasySectionGenre()
                .pause()
                .callRecentPage()
                .pause()
                .callFirstBook()
                .pause()
                .callPopularPage()
                .pause()
                .callFirstBook()
                .pause()
                .callAuthorsPage()
                .pause()
                .callFirstAuthorsItem()
                .pause();
        assertTrue(driver.getPageSource().contains("Биография"));
    }
}