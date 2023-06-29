package com.example.bookshopapp.selenium;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class MainMenuPages {
    private static final String URL = "http://localhost:8085/";
    private final ChromeDriver driver;

    public MainMenuPages(ChromeDriver driver) {
        this.driver = driver;
    }

    public MainMenuPages callPage() {
        driver.get(URL);
        return this;
    }

    public MainMenuPages pause() throws InterruptedException {
        Thread.sleep(500);
        return this;
    }

    public MainMenuPages callFirstBook() {
        WebElement element = driver.findElementByXPath("//strong[@class='Card-title']/a[contains(@href,'book')]");
        element.click();
        return this;
    }

    public MainMenuPages callFantasySectionGenre() {
        WebElement element = driver.findElementByXPath("//a[text()='Фантастика']");
        element.click();
        return this;
    }

    public MainMenuPages callFirstAuthorsItem() {
        WebElement element = driver.findElementByXPath("//div[@class='Authors-item']/a[contains(@href,'author')]");
        element.click();
        return this;
    }

    public MainMenuPages callGenrePage() {
        WebElement element = driver.findElementByXPath("//a[text()='Жанры']");
        element.click();
        return this;
    }

    public MainMenuPages callRecentPage() {
        WebElement element = driver.findElementByXPath("//a[text()='Новинки']");
        element.click();
        return this;
    }

    public MainMenuPages callPopularPage() {
        WebElement element = driver.findElementByXPath("//a[text()='Популярное']");
        element.click();
        return this;
    }

    public MainMenuPages callAuthorsPage() {
        WebElement element = driver.findElementByXPath("//a[text()='Авторы']");
        element.click();
        return this;
    }
}
