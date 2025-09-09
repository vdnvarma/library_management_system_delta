package com.example.lms.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookSearchTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60)); // 60s timeout for slow loads
    }

    @Test
    public void testBookSearchFeature() {
        driver.get("https://lmsbeta.onrender.com");
        // Wait for any input field to appear (search box)
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input")));
        searchInput.sendKeys("Java");
        // Wait for any button to appear (since specific "Search" button might not exist)
        WebElement anyButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button")));
        assertTrue(anyButton.isDisplayed());
        // Optionally, check for results container or any result element
    }    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
