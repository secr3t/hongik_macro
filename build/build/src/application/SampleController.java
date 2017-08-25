package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SampleController implements Initializable {
	@FXML private TextField id;
	@FXML private PasswordField pw;
	@FXML private Button run;
	@FXML private Button stop;
	@FXML private Button exit;
	@FXML private Button login;
	@FXML private ChoiceBox<String>option;
	
	private final String URL ="http://sugang.hongik.ac.kr/cn1000.jsp";
	private boolean runFlag = false;
	private WebDriver driver = null;
	private Dimension windowSize;
	private int hit=0;
	private final ObservableList<String> options = FXCollections.observableArrayList("초기 수강신청", "담은과목 성공할 때까지 무한반복");
	MyThread1 thread1 = new MyThread1();
	MyThread2 thread2 = new MyThread2();
	
	class MyThread1 extends Thread{
		@Override
		public void run() {
			//담은과목 수강신청하기 링크 클릭.
			WebElement sugangBtn = 
			driver.findElement(By.cssSelector("body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(1) > td > table > tbody > tr:nth-child(19) > td > a"));
			try {
				sugangBtn.click();
			} catch (Exception e) {
				scrollIntoView(driver, sugangBtn);
				sugangBtn.click();
			}

			//수강신청 클릭
			WebElement startSugang =
					driver.findElement(By.cssSelector("body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(3) > div:nth-child(2) > table > tbody > tr > td > form > table > tbody > tr:nth-child(24) > td > input:nth-child(2)"));
			try {
				startSugang.click();
			} catch (Exception e) {
				scrollIntoView(driver, startSugang);
				startSugang.click();
			}
			
			while(runFlag) {
				try {
					WebDriverWait wait = new WebDriverWait(driver, 1);
					wait.until(ExpectedConditions.alertIsPresent());
					org.openqa.selenium.Alert sugangAlert = driver.switchTo().alert();
					String message = sugangAlert.getText();
					System.out.println(message);
					sugangAlert.accept();
					System.out.println("확인 누른 횟수" + ++hit);
				} catch (Exception e) {
					
				}
			}
		}
	}
	class MyThread2 extends Thread{
		@Override
		public void run() {
			while(runFlag) {
				//담은과목 수강신청하기 링크 클릭.
				WebElement sugangBtn = 
				driver.findElement(By.cssSelector("body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(1) > td > table > tbody > tr:nth-child(19) > td > a"));
				try {
					sugangBtn.click();
				} catch (Exception e) {
					scrollIntoView(driver, sugangBtn);
					sugangBtn.click();
				}

				//수강신청 클릭
				WebElement startSugang =
						driver.findElement(By.cssSelector("body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(3) > div:nth-child(2) > table > tbody > tr > td > form > table > tbody > tr:nth-child(24) > td > input:nth-child(2)"));
				try {
					startSugang.click();
				} catch (Exception e) {
					scrollIntoView(driver, startSugang);
					startSugang.click();
				}
				for(int i = 0; i<10; i++) {
					try {
						WebDriverWait wait = new WebDriverWait(driver, 1);
						wait.until(ExpectedConditions.alertIsPresent());
						org.openqa.selenium.Alert sugangAlert = driver.switchTo().alert();
						String message = sugangAlert.getText();
						System.out.println(message);
						sugangAlert.accept();
						System.out.println("확인 누른 횟수" + ++hit);
					} catch (Exception e) {
						
					}
				}
			}
		}
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
		option.setItems(options);
		option.getSelectionModel().select(0);
		load();
		binding();
		run.setDisable(true);
		stop.setDisable(true);
	}

	public void binding() {
		bindRun();
		bindStop();
		bindExit();
		bindLogin();
	}
 
	public void bindRun() {
		run.setOnAction(e-> {
			try {
				start(e);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
	}
	public void bindStop() {
		stop.setOnAction(e->{
			try {
				stop(e);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
	}
	public void bindExit() {
		exit.setOnAction(e->{
				try {
					exit(e);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		});
	}
	public void bindLogin() {
		login.setOnAction(e->{
			try {
				login(e);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
	}
	
	// to prevent blocking UI while run use Threads
	public void start(ActionEvent e) throws InterruptedException {
		int selectedOption = option.getSelectionModel().getSelectedIndex();
		if(selectedOption == 1) {
			if(!thread1.isAlive())
				thread1.start();
		} else {
			
		}

		runFlag = true;
	}
	public void stop(ActionEvent e) throws InterruptedException {
		runFlag = false;
	}
	public void exit(ActionEvent e) throws InterruptedException {
		driver.quit();
		Stage stage = (Stage) exit.getScene().getWindow();
		stage.close();
	}
	public void login(ActionEvent e) throws InterruptedException {
		if(!adminIsNull()) {
			run.setDisable(false);
			stop.setDisable(false);
			hongikLogin();
		} else {
			loginfailAlert();
		}
	}
	
	public void load() {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		windowSize = driver.manage().window().getSize();
		driver.manage().window().setPosition(new Point(0, 0));   
		driver.manage().window().setSize(new Dimension(windowSize.getWidth()/2, windowSize.getHeight()));
		driver.get(URL);
	}

	public void hongikLogin() {
		driver.findElement(By.cssSelector("body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1) > td:nth-child(2) > input")).sendKeys(id.getText());
		driver.findElement(By.cssSelector("body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(3) > td:nth-child(2) > input")).sendKeys(pw.getText());
		WebElement loginBtn = 
				driver.findElement(By.cssSelector("body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(4) > td > input"));
		try {
			loginBtn.click();
		} catch (Exception e) {
			scrollIntoView(driver, loginBtn);
			loginBtn.click();
		}
	}

	public Object runScript(WebDriver driver, String script, WebElement target) {
		/*
		 * to run script shorter
		*/
		return ((JavascriptExecutor) driver).executeScript(script, target);
	}
	
	public void scrollIntoView(WebDriver driver, WebElement element) {
	runScript(driver, "arguments[0].scrollIntoView(true)", element);
	}
	
	public boolean adminIsNull() {
		if(id.getText().isEmpty() || pw.getText().isEmpty())
			return true;
		else
			return false;
	}
	public void loginfailAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("로그인 실패");
		alert.setHeaderText("id 또는 pw가 비어있습니다.");
		alert.setContentText("id, pw를 모두 채워주십시오. 바보얌");
		alert.showAndWait().ifPresent(rs -> {
		    if (rs == ButtonType.OK) {
		        System.out.println("Pressed OK.");
		    }
		});
	}
	
}
