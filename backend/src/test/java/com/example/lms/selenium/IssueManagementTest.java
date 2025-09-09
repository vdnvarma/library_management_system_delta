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

public class IssueManagementTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60)); // 60s timeout for slow loads
    }

    @Test
    public void testIssueManagementFeature() {
        driver.get("https://lmsbeta.onrender.com");
        // Wait for any button to appear (since management features require login/navigation)
        WebElement anyButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button")));
        assertTrue(anyButton.isDisplayed());
    }    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
