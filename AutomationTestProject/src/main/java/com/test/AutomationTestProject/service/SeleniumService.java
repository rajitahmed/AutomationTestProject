package com.test.AutomationTestProject.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class SeleniumService {

    private final FinicityService finicityService;

    public SeleniumService(FinicityService finicityService) {
        this.finicityService = finicityService;
    }

    public Mono<String> processConnectLink() {
        String connectLink = finicityService.getGeneratedLink();
        
        if (connectLink == null || connectLink.isEmpty()) {
            log.error("Cannot process connect link: link is NULL or empty!");
            return Mono.just("❌ Cannot process connect link: No link available");
        }

        return Mono.fromCallable(() -> {
            log.info("Starting Selenium automation for connect link: {}", connectLink);
            
            // Setup WebDriver
            WebDriverManager.chromedriver().setup();
            
            
            WebDriver driver = null;
            try {
                driver = new ChromeDriver();
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                
                // Navigate to the connect link
                driver.get(connectLink);
                log.info("Navigated to connect link");
                
                // Step 1: Click the Next button
                WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'Next')]")));
                nextButton.click();
                log.info("Clicked on Next button");
                
                // Step 2: Search for FinBank
                WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//input[@name='Search for your bank']")));
                searchInput.sendKeys("finbank");
                log.info("Entered 'finbank' in search field");
                
                // Click on FinBank option
                WebElement finBankOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[text()='FinBank']")));
                finBankOption.click();
                log.info("Selected FinBank from dropdown");
                
                // Step 3: Click Next button again
                WebElement nextButton2 = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button/div[contains(text(), 'Next')]")));
                nextButton2.click();
                log.info("Clicked on Next button after selecting bank");
                
                // Step 4: Enter credentials
                WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//input[@name='Banking Userid']")));
                usernameField.sendKeys("profile03");
                log.info("Entered username: profile03");
                
                WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//input[@name='Banking Password']")));
                passwordField.sendKeys("profile03");
                log.info("Entered password: profile03");
                
                // Step 6: Submit
                WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button/span[contains(text(), 'Submit')]")));
                submitButton.click();
                log.info("Clicked on Submit button");

                // Step 5: Select checking account and save
                WebElement checkingOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(text(), 'Checking')]")));
                checkingOption.click();
                log.info("Selected Checking account option");
                
                WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'Save')]")));
                saveButton.click();
                log.info("Clicked on Save button");
                
                // Step 6: Submit
                WebElement submitButton2 = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'Submit')]")));
                submitButton2.click();
                log.info("Clicked on Submit button");
                
                
                log.info("Selenium automation completed successfully");
                return "✅ Finicity Connect flow completed successfully";
                
            } catch (Exception e) {
                log.error("Error during Selenium automation: {}", e.getMessage(), e);
                return "❌ Selenium automation failed: " + e.getMessage();
            } finally {
                // Close the browser
                if (driver != null) {
                    driver.quit();
                    log.info("Browser closed");
                }
            }
        });
    }
}
