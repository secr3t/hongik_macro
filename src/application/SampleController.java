package application;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.client.utils.DateUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javafx.application.Platform;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SampleController implements Initializable {
	@FXML
	private TextField id;
	@FXML
	private TextField cnttxt;
	@FXML
	private TextField serverTime;
	@FXML
	private TextField sysTime;
	@FXML
	private PasswordField pw;
	@FXML
	private Button run;
	@FXML
	private Button stop;
	@FXML
	private Button exit;
	@FXML
	private Button login;
	@FXML
	private ChoiceBox<String> option;
	@FXML
	private ChoiceBox<String> option2;

	private final String targetURL = "http://sugang.hongik.ac.kr/cn1000.jsp";
	private boolean runFlag = false;
	private boolean loggedin = false;
	private WebDriver driver = null;
	private Dimension windowSize;
	private int hit = 0;
	private int cnt;
	private final ObservableList<String> options = FXCollections.observableArrayList("초기 수강신청", "담은과목 성공할 때까지 무한반복");
	private final ObservableList<String> options2 = FXCollections.observableArrayList("9시 수강신청", "2시 수강신청");
	private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private URL obj;
	private URLConnection conn;

	MyThread1 thread1 = new MyThread1();
	MyThread2 thread2 = new MyThread2();
	MyThread3 thread3 = new MyThread3();

	class MyThread1 extends Thread {
		@Override
		public void run() {
			// 담은과목 수강신청하기 링크 클릭.
			int selectedOption = option2.getSelectionModel().getSelectedIndex();
			if (selectedOption == 0)	// 9시 수강신청
				while (true) {
					if(!loggedin && serverTime.getText().equals("08:58:00")) {
						hongikLogin();
						loggedin = true;
					}
					if (serverTime.getText().equals("09:00:00"))
						break;
				}
			else	// 2시 수강신청
				while (true) {
					if(!loggedin && serverTime.getText().equals("13:58:00")) { 	// 15초 전 로그인.
						hongikLogin();
						loggedin = true;
					}
					if (serverTime.getText().equals("14:00:00")) // test : 서버시간 16시 35분에 수강신청 시작.
						break;
				}
			WebDriverWait loginWait = new WebDriverWait(driver, 300);
			loginWait.until(ExpectedConditions.urlContains("p_contm"));
			WebElement sugangBtn = driver.findElement(By.cssSelector(
					"body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(1) > td > table > tbody > tr:nth-child(19) > td > a"));
			try {
				sugangBtn.click();
			} catch (Exception e) {
				scrollIntoView(driver, sugangBtn);
				sugangBtn.click();
			}

			// 수강신청 method 실행
			((JavascriptExecutor) driver).executeScript("sel_check();");

			while (runFlag) {
				try {
					WebDriverWait wait = new WebDriverWait(driver, 1);
					wait.until(ExpectedConditions.alertIsPresent());
					org.openqa.selenium.Alert sugangAlert = driver.switchTo().alert();
					String almsg = sugangAlert.getText();
					System.out.println(almsg);
					sugangAlert.accept();
					System.out.println("신청 실패횟수" + ++hit);
				} catch (Exception e) {

				}
			}
		}
	}

	class MyThread2 extends Thread {
		@Override
		public void run() {
			String loggedInUrl = driver.getCurrentUrl();
			while (runFlag) {
				driver.get(loggedInUrl);
				// 담은과목 수강신청하기 링크 클릭.
				WebElement sugangBtn = driver.findElement(By.cssSelector(
						"body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(1) > td > table > tbody > tr:nth-child(19) > td > a"));
				try {
					sugangBtn.click();
				} catch (Exception e) {
					scrollIntoView(driver, sugangBtn);
					sugangBtn.click();
				}

				// 수강신청 method 실행

				List<WebElement> failed = driver.findElements(By.cssSelector("input[name='chk']"));
				cnt = failed.size();
				cnttxt.setText(String.valueOf(cnt));
				((JavascriptExecutor) driver).executeScript("sel_check();");

				for (int i = 0; i < cnt; i++) { // 수강신청 실패한 갯수만큼만 돌리기.
					try {
						WebDriverWait wait = new WebDriverWait(driver, 1);
						wait.until(ExpectedConditions.alertIsPresent());
						org.openqa.selenium.Alert sugangAlert = driver.switchTo().alert();
						String almsg = sugangAlert.getText();
						System.out.println(almsg);
						sugangAlert.accept();
					} catch (Exception e) {

					}
				}
			}
		}
	}

	class MyThread3 extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					obj = new URL("http://sugang.hongik.ac.kr");
					conn = obj.openConnection();
					String serverDate = conn.getHeaderFields().get("Date").get(0);
					Date serverDateObj = DateUtils.parseDate(serverDate);
					serverTime.setText(sdf.format(serverDateObj));
					sysTime.setText(sdf.format(new Date()));
					Thread.sleep(300);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
		ChromeOptions optionss = new ChromeOptions();
		optionss.addArguments("no-sandbox");
		option.setItems(options);
		option.getSelectionModel().select(0);
		option2.setItems(options2);
		option2.getSelectionModel().select(0);

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
		run.setOnAction(e -> {
			try {
				start(e);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
	}

	public void bindStop() {
		stop.setOnAction(e -> {
			try {
				stop(e);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
	}

	public void bindExit() {
		exit.setOnAction(e -> {
			try {
				exit(e);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
	}

	public void bindLogin() {
		login.setOnAction(e -> {
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
		if (selectedOption == 0) {
			if (!thread1.isAlive())
				thread1.start();
		} else {
			if (!thread2.isAlive())
				thread2.start();
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

		if (!thread3.isAlive())
			thread3.start();
		if (option.getSelectionModel().getSelectedIndex() == 0) {
			start(e);
		} else {
			if (!adminIsNull()) {
				run.setDisable(false);
				stop.setDisable(false);
				hongikLogin();
			} else {
				loginfailAlert();
			}
		}
	}

	public void load() {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		windowSize = driver.manage().window().getSize();
		driver.manage().window().setPosition(new Point(0, 0));
		driver.manage().window().setSize(new Dimension(windowSize.getWidth() / 2, windowSize.getHeight()));
		driver.get(targetURL);

	}

	public void hongikLogin() {
		driver.findElement(By.cssSelector(
				"body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1) > td:nth-child(2) > input"))
				.sendKeys(id.getText());
		driver.findElement(By.cssSelector(
				"body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(3) > td:nth-child(2) > input"))
				.sendKeys(pw.getText());
		WebElement loginBtn = driver.findElement(By.cssSelector(
				"body > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(1) > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(4) > td > input"));
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
		if (id.getText().isEmpty() || pw.getText().isEmpty())
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
