package com.example.cgpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end UI test: opens the real app in headless Chrome, picks a grade
 * for every pre-filled course, submits the form, and checks the result page.
 *
 * Requires Google Chrome to be installed on the machine running the test.
 * Selenium Manager (bundled with selenium-java 4.6+) downloads a matching
 * chromedriver automatically - no manual driver setup needed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CgpaUiSeleniumTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1280,1024");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void fillingEveryGradeAndSubmittingShowsAnSgpaResult() {
        driver.get("http://localhost:" + port + "/");
        assertTrue(driver.getTitle().contains("calculator"));

        List<WebElement> gradeDropdowns = driver.findElements(By.cssSelector("select[name=grade]"));
        assertTrue(gradeDropdowns.size() > 0, "expected the pre-filled course rows to be on the page");

        for (WebElement dropdown : gradeDropdowns) {
            new Select(dropdown).selectByValue("A");
        }

        driver.findElement(By.cssSelector("button.primary")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("h1"), "Your SGPA"));

        String sgpaText = driver.findElement(By.cssSelector("main.sheet > p.cgpa > span")).getText();
        double sgpa = Double.parseDouble(sgpaText);
        // Every course graded "A" (8 points) means the SGPA must be exactly 8.
        assertEquals(8.0, sgpa, 0.001);
    }

    @Test
    void resetLinkRestoresTheDefaultCourseList() {
        driver.get("http://localhost:" + port + "/");

        // Change a course name, then follow Reset.
        WebElement firstName = driver.findElements(By.cssSelector("input[name=name]")).get(0);
        firstName.clear();
        firstName.sendKeys("Something Else");

        driver.findElement(By.linkText("Reset")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name=name]")));

        String restoredName = driver.findElements(By.cssSelector("input[name=name]")).get(0)
                .getAttribute("value");
        assertEquals("Basics of Financial Services", restoredName);
    }
}
