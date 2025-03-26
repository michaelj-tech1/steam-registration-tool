package org.gh;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;

import org.apache.commons.lang3.tuple.Triple;
import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.gh.Email.getSteamLink;


public class Main {
    private static final String SETTINGS_FILE = "settings.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<BrowserContext> activeBrowserContexts = Collections.synchronizedList(new ArrayList<>());

    private static final List<Thread> activeThreads = Collections.synchronizedList(new ArrayList<>());

    public static volatile boolean isStopped = false;
    private static boolean useProxies;
    private static String numberOfAccounts;
    private static String customUsername;



    public static void stopAllThreadsAndCloseTabs() {
        isStopped = true;

        for (Thread thread : activeThreads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
        activeThreads.clear();

        for (BrowserContext context : activeBrowserContexts) {
            context.close();
        }
        activeBrowserContexts.clear();
    }


    public static void saveSettings(Settings settings) {
        try {
            FileWriter writer = new FileWriter(SETTINGS_FILE);
            GSON.toJson(settings, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Settings loadSettings() {
        Settings settings = new Settings();
        try {
            FileReader reader = new FileReader(SETTINGS_FILE);
            settings = GSON.fromJson(reader, Settings.class);
            reader.close();
        } catch (IOException e) {
            System.out.println("");
        }
        return settings;
    }



    public static String generatePassword() {
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String symbols = "!#$^&*?";
        String allChars = lowerCase + upperCase + numbers + symbols;

        int minLength = 13;
        int maxLength = 15;

        Random random = new Random();
        int length = random.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder password = new StringBuilder(length);

        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(symbols.charAt(random.nextInt(symbols.length())));

        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        List<Character> charList = password.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(charList);
        char[] scrambledArray = new char[charList.size()];
        for (int i = 0; i < charList.size(); i++) {
            scrambledArray[i] = charList.get(i);
        }

        return new String(scrambledArray);
    }

    private static void saveInfo(String email, String password, String username1, String password1, String phoneNumber) {
        try {
            String fileName = "SteamAccounts.txt";
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            String currentDate = dateFormat.format(date);
            bufferedWriter.write(email + ":" + "EmailPassword:"+ password1 + ":" + username1 + ":" + password+ ":" +  phoneNumber + ":" + currentDate + "\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEmails(String email, String password) {
        try {
            String fileName = "UnusedEmails.txt";
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(email + ":" + password + "\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void onButtonClick(boolean useProxies, String numberOfAccounts, String customUsername, String tabsToRun) {
        Main.isStopped = false;
        Main.useProxies = useProxies;
        Main.numberOfAccounts = numberOfAccounts;
        Main.customUsername = customUsername;

        int numAccounts = Integer.parseInt(numberOfAccounts);
        int numTabs = Integer.parseInt(tabsToRun);

        ExecutorService executor = Executors.newFixedThreadPool(numTabs);

        for (int i = 0; i < numAccounts; i++) {
            executor.submit(new AccountCreatorWorker(1));
        }

        executor.shutdown();
    }


    static class AccountCreatorWorker extends SwingWorker<Void, String> {
        private final int accountsToCreate;

        AccountCreatorWorker(int numAccounts) {
            this.accountsToCreate = numAccounts;
        }

        @Override
        protected Void doInBackground() throws Exception {
            for (int i = 0; i < accountsToCreate; i++) {
                while (!isCancelled()) {
                    try {
                        createAccounts();
                        break;
                    } catch (Exception ex) {
                        publish("Attempt to create account failed, opening new tab to try again. " + ex);

                    }
                }
            }
            return null;
        }



        @Override
        protected void process(List<String> messages) {
            for (String message : messages) {
                MainUi.getConsoleTextArea().append(message + "\n");
            }
        }

        public <Pair> void createAccounts() {


            if (Main.isStopped) {
                stopAllThreadsAndCloseTabs();

                return;
            }

            try (Playwright playwright = Playwright.create()) {
                String extensionPath = System.getProperty("user.dir") + "\\chromium_automation";
                Path tempProfileDir = Files.createTempDirectory("playwright_fake_profile");
                System.out.println("Using temporary profile directory: " + tempProfileDir);

                String chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";


                BrowserType.LaunchPersistentContextOptions contextOptions = new BrowserType.LaunchPersistentContextOptions()
                        .setViewportSize(500, 500)
                        .setHeadless(false)
                        .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 17_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148")
                        .setArgs(Arrays.asList(
                                "--disable-extensions-except=" + extensionPath,
                                "--load-extension=" + extensionPath,
                                "--lang=en-US",
                                "--disable-blink-features=AutomationControlled",
                                "--enable-automation=false",
                                "--no-default-browser-check"
                        ));

                ProxyInfo proxyInfo = null;
                if (useProxies) {
                    ProxyHandler proxyHandler = new ProxyHandler();
                    proxyInfo = proxyHandler.getNextProxy();
                    if (proxyInfo != null) {
                        contextOptions.setProxy(proxyInfo.getProxy());
                    }
                }

                BrowserContext context = playwright.chromium().launchPersistentContext(tempProfileDir, contextOptions);
                activeBrowserContexts.add(context);
                context.addInitScript("window.navigator.webdriver = undefined;");

                Page mainPage = context.newPage();
                publishProgress("Opening up Steam... ");
                mainPage.navigate("https://store.steampowered.com/join");

                mainPage.setDefaultTimeout(120000);
                for (Page page : context.pages()) {
                    if (page != mainPage) {
                        page.close();
                    }
                }
                mainPage.waitForSelector("//*[@id='email']");


                String email1 = "";
                String password1 = "";
                String id1 = "";

                if (MainUi.isEmailsDotTxtSelected()) {

                    publishProgress("Using email from .txt");
                    Optional<String[]> result = EmailsReader.getFirstEmailAndPassword();
                    String[] emailAndPassword = result.get();
                    email1 = emailAndPassword[0];
                    password1 = emailAndPassword[1];
                    System.out.println("Email and password: " + email1 + " : " + password1 );
                } else {
                    String apiKey = MainUi.getApiKey();
                    publishProgress("Requesting email...");
                    Map<String, String> emailInfo = Email.emailRequest(apiKey);
                    email1 = emailInfo.get("email");
                    id1 = emailInfo.get("id");
                    password1 = emailInfo.get("password");
                }


                mainPage.evaluate("document.getElementById('email').value = '" + email1 + "';");
                mainPage.waitForTimeout(3000);
                mainPage.evaluate("document.getElementById('reenter_email').value = '" + email1 + "';");
                mainPage.waitForTimeout(2000);
                mainPage.evaluate("document.getElementById('i_agree_check').click();");

//                ElementHandle frameElement = mainPage.waitForSelector("iframe[title='reCAPTCHA']");
//
//
//                Frame iframe = frameElement.contentFrame();
//                iframe.waitForSelector("span.recaptcha-checkbox[aria-checked='true']");

                mainPage.waitForSelector(".capsolver-solver-info:has-text('Captcha solved!')");
                publishProgress("Captcha is done ");
                publishProgress("Clicking terms ");


                boolean isCustomCountrySelected = MainUi.isCustomCountrySelected();
                if (isCustomCountrySelected) {
                    String customCountryCode1 = MainUi.getCustomCountryCode();
                    mainPage.selectOption("#country", customCountryCode1);
                }else{
                    String customCountryCode = "US";
                    mainPage.selectOption("#country", customCountryCode);
                }


                mainPage.waitForTimeout(2000);
                mainPage.evaluate("document.evaluate(\"//span[text()='Continue']\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click();");


                mainPage.waitForTimeout(3000);
                try {
                    int maxAttempts = 5;
                    boolean emailConfirmed = false;

                    for (int attempt = 1; attempt <= maxAttempts; attempt++) {

                        try {
                            ElementHandle emailConfirmationText = mainPage.waitForSelector("text=Verify Your Email",
                                    new Page.WaitForSelectorOptions().setTimeout(5000));

                            if (emailConfirmationText != null) {
                                publishProgress("Successfully moved to email confirmation.");
                                System.out.println("Successfully moved to email confirmation. Exiting loop.");
                                emailConfirmed = true;
                                break;
                            }
                        } catch (TimeoutError e) {
                            System.out.println("Email verification text not found in attempt " + attempt + ". Retrying...");
                            publishProgress("Captcha error or still getting captcha attempt: " + attempt);
//                            ElementHandle iframe1 = mainPage.waitForSelector("iframe[title='reCAPTCHA']");
//                            Frame iframeFrame1 = iframe1.contentFrame();
//                            iframeFrame1.click("#recaptcha-anchor");
//                            iframeFrame1.waitForSelector("span.recaptcha-checkbox-checked[aria-checked='true']");
                            mainPage.waitForSelector(".capsolver-solver-info:has-text('Captcha solved!')");
                            mainPage.waitForTimeout(1000);
                            ElementHandle continueButton = mainPage.waitForSelector("//button[@id='createAccountButton']//span[contains(text(),'Continue')]",
                                    new Page.WaitForSelectorOptions().setTimeout(3000));
                            if (continueButton != null) {
                                continueButton.click();
                            }
                        } catch (Exception e) {
                            System.out.println("Unexpected error during email verification check: " + e.toString());
                            e.printStackTrace();
                        }
                    }
                    mainPage.waitForTimeout(2000);
                    if (!emailConfirmed) {
                        publishProgress("Total attempts reached closing page now... " + "Max attempts: " + maxAttempts);
                        if(MainUi.isEmailsDotTxtSelected()){
                            saveEmails(email1,password1);
                        }
                        context.close();
                        mainPage.close();
                        createAccounts();
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.toString());
                    e.printStackTrace();
                }
                mainPage.waitForTimeout(1000);
                String apiKey = MainUi.getApiKey();

                if(MainUi.isEmailsDotTxtSelected()) {
                    EmailReader emailReader = new EmailReader(email1, password1);
                    String link = emailReader.getSteamVerificationLink();
                    if (link != null) {
                        Object result = mainPage.evaluate("link => {" +
                                "  return fetch(link)" +
                                "    .then(response => response.text())" +
                                "    .then(data => { return data; });" +
                                "}", link);

                        System.out.println("Fetched data: " + result);


                    } else {
                        System.out.println("Verification link not found.");
                        mainPage.close();
                        context.close();
                        Thread.sleep(500);
                        createAccounts();
                        publishProgress("Couldn't access the account or get verify link.");
                    }
                }else{

                    System.out.println(apiKey + " " + id1);
                    String linkRegex = "https://store\\.steampowered\\.com/account/newaccountverification\\?stoken=.*?&creationid=.*?(?=\"|&)";


                    String steamLink = getSteamLink(apiKey, id1, linkRegex);
                    System.out.println("Making request to: " + steamLink);
                    if (steamLink != null) {
                        Object result = mainPage.evaluate("link => {" +
                                "  return fetch(link)" +
                                "    .then(response => response.text())" +
                                "    .then(data => { return data; });" +
                                "}", steamLink);

                        System.out.println("Fetched data: " + result);
                        System.out.println("Email1: " + email1);

                    }
                    Email.cancelMailbox(apiKey,id1);
                }
                mainPage.locator("//*[@id=\"accountname\"]").click();
                Random rand = new Random();
                int num1 = rand.nextInt(10000);
                String username4 = customUsername + num1;
                mainPage.type("//*[@id=\"accountname\"]", username4, new Page.TypeOptions().setDelay(60));

                mainPage.locator("//*[@id=\"password\"]").click();
                String password2 = generatePassword();
                System.out.println(password2);
                mainPage.type("//*[@id=\"password\"]", password2, new Page.TypeOptions().setDelay(60));
                mainPage.locator("//*[@id=\"reenter_password\"]").click();
                mainPage.type("//*[@id=\"reenter_password\"]", password2, new Page.TypeOptions().setDelay(60));
                mainPage.waitForTimeout(1000);
                mainPage.click("//*[@id=\"createAccountButton\"]");
                mainPage.waitForTimeout(6000);





                mainPage.navigate("https://store.steampowered.com/twofactor/manage");
                mainPage.waitForTimeout(4000);
                String title1 = mainPage.title();
                if (title1.equals("Sign In")) {
                    mainPage.waitForSelector("xpath=//input[@type='text' and contains(@class, 'newlogindialog_TextInput_2eKVn')]");
                    mainPage.fill("xpath=//input[@type='text' and contains(@class, 'newlogindialog_TextInput_2eKVn')]", username4);
                    mainPage.waitForSelector("xpath=//input[@type='password' and contains(@class, 'newlogindialog_TextInput_2eKVn')]");
                    mainPage.fill("xpath=//input[@type='password' and contains(@class, 'newlogindialog_TextInput_2eKVn')]", password2);
                    mainPage.waitForTimeout(1000);
                    mainPage.click(".newlogindialog_SubmitButton_2QgFE");
                }

                try {
                    mainPage.waitForSelector("#rejectAllButton", new Page.WaitForSelectorOptions().setTimeout(5000));
                    mainPage.click("#rejectAllButton");
                } catch (Exception e) {
                    System.out.println("Couldnt click the button");
                }

                mainPage.click("input[type='radio'][name='email_authenticator_check'][id='email_authenticator_check']");
                mainPage.waitForTimeout(1000);

                String title2 = mainPage.title();
                if (title2.equals("Sign In")) {
                    mainPage.waitForSelector("xpath=//input[@type='text' and contains(@class, 'newlogindialog_TextInput_2eKVn')]");
                    mainPage.fill("xpath=//input[@type='text' and contains(@class, 'newlogindialog_TextInput_2eKVn')]", username4);

                    mainPage.waitForSelector("xpath=//input[@type='password' and contains(@class, 'newlogindialog_TextInput_2eKVn')]");
                    mainPage.fill("xpath=//input[@type='password' and contains(@class, 'newlogindialog_TextInput_2eKVn')]", password2);
                    mainPage.waitForTimeout(1000);
                    mainPage.click(".newlogindialog_SubmitButton_2QgFE");
                }
                mainPage.waitForTimeout(1000);
                mainPage.click("input[type='radio'][name='none_authenticator_check'][id='none_authenticator_check']");
                mainPage.waitForTimeout(1000);


                mainPage.click("a.btnv6_green_white_innerfade.btn_medium.button:has-text('Disable Steam Guard')");

                if(MainUi.isEmailsDotTxtSelected()) {
                    System.out.println("Email and password: " + email1 + " : " + password1 );
                    SteamGuardEmailFetcher fetcher1 = new SteamGuardEmailFetcher(email1, password1);
                    String steamGuardLink = fetcher1.getSteamGuardLink();
                    if (steamGuardLink != null) {
                        Object result = mainPage.evaluate("link => {" +
                                "  return fetch(link)" +
                                "    .then(response => response.text())" +
                                "    .then(data => { return data; });" +
                                "}", steamGuardLink);

                        System.out.println("Fetched data: " + result);


                    } else {
                        System.out.println("Verification link not found.");
                        mainPage.close();
                        context.close();
                        Thread.sleep(500);
                        createAccounts();
                        publishProgress("Couldn't access the account or get verify link.");
                    }
                }else {
                    System.out.println("Email1 is: " + email1);

                    String id = Email.reorderMailbox(email1,apiKey);
                    String linkRegex = "https://store\\.steampowered\\.com/account/steamguarddisableverification\\?stoken=.*?&steamid=.*?\"";
                    String steamLink = getSteamLink(apiKey, id, linkRegex);


                    System.out.println("Making request to: " + steamLink);
                    if (steamLink != null) {
                        // Use the link to make a GET request
                        Object result = mainPage.evaluate("link => {" +
                                "  return fetch(link)" +
                                "    .then(response => response.text())" +
                                "    .then(data => { return data; });" +
                                "}", steamLink);

                        System.out.println("Fetched data: " + result);

                    }
                    System.out.println("Made the request");
                    Email.cancelMailbox(apiKey,id1);
                }

//                Page newPage1 = context.newPage();
//                newPage1.navigate(steamGuardLink);
//                newPage1.close();
                mainPage.waitForTimeout(5000);
                publishProgress("Account created! Saving the information to .txt file ");
                String phoneNumber = null;
                try {
                    if (MainUi.isSmsApiChecked()) {
                        String smsApiKey = MainUi.getSmsApiKey();
                        mainPage.navigate("https://store.steampowered.com/phone/add");

                        try {
                            mainPage.waitForTimeout(1000);
                            String title = mainPage.title();
                            if (title.equals("Sign In")) {
                                System.out.println("Page Title: " + title);
                                mainPage.waitForSelector("xpath=//input[@type='text' and contains(@class, 'newlogindialog_TextInput_2eKVn')]");
                                mainPage.fill("xpath=//input[@type='text' and contains(@class, 'newlogindialog_TextInput_2eKVn')]", username4); // replace "username" with your username
                                mainPage.waitForSelector("xpath=//input[@type='password' and contains(@class, 'newlogindialog_TextInput_2eKVn')]");
                                mainPage.fill("xpath=//input[@type='password' and contains(@class, 'newlogindialog_TextInput_2eKVn')]", password2); // replace "password" with your password
                                mainPage.waitForTimeout(1000); // wait for 5 seconds
                                mainPage.click(".newlogindialog_SubmitButton_2QgFE");
                                mainPage.waitForLoadState(LoadState.LOAD);
                                String pageContent = mainPage.content();
                                mainPage.waitForTimeout(2000);

                                if (pageContent.contains("<div class=\"newlogindialog_FormError_1Mcy9\">Please check your password and account name and try again.</div>")) {
                                    System.out.println("Error message 'Please check your password and account name and try again.' appeared on the page.");
                                    mainPage.close();
                                    createAccounts();
                                } else {
                                    System.out.println("Error message did not appear on the page.");
                                }
                            } else {
                                System.out.println("Title does not match");
                            }
                        } catch (Exception e) {
                            System.out.println("An error occurred: " + e.getMessage());
                        }

                        try {
                            mainPage.waitForSelector("#rejectAllButton", new Page.WaitForSelectorOptions().setTimeout(10000));
                            mainPage.click("#rejectAllButton");
                        } catch (Exception e) {
                            System.out.println("Couldnt click the button");
                        }

                        String country = MainUi.getCountry();
                        Triple<String, String, String> pvaResult = null;

                        String id = null;
                        String countryCode = null;

                        if (MainUi.isSmsPvaChecked()) {
                            pvaResult = SmsPvaRequest.getSmsNumber(smsApiKey, country);
                            phoneNumber = pvaResult.getLeft();
                            id = pvaResult.getMiddle();
                            countryCode = pvaResult.getRight();

                        } else if (MainUi.is5simChecked()) {
                            String operator = MainUi.getOperatorText();
                            String[] result = FiveSimRequest.getNumberAndCountryCode(country, operator, smsApiKey);
                            phoneNumber = result[0];
                            id = result[1];
                        } else if (MainUi.isSmsHubSelected()) {

                            String[] result = SmsHubRequest.getNumber(smsApiKey, country);
                            phoneNumber = result[1];
                            id = result[0];
                        }

                        publishProgress("Got the phone number " + phoneNumber);


                        if (MainUi.isSmsPvaChecked()) {
                            mainPage.locator("//*[@id=\"tel_entry\"]").fill(countryCode + phoneNumber);
                            mainPage.waitForTimeout(1000);
                            mainPage.click("xpath=//span[@onclick='handleNextButton()']");
                            if(MainUi.isEmailsDotTxtSelected()) {
                                PhoneEmail phoneEmail = new PhoneEmail(email1, password1);
                                phoneEmail.processSteamVerificationLink();
                            }else{

                                String id2 = Email.reorderMailbox(email1,apiKey);
                                String linkRegex = "https://store\\.steampowered\\.com/phone/ConfirmEmailForAdd\\?stoken=.*?&steamid=[^\\s\"']++";

                                String steamLink = getSteamLink(apiKey, id2, linkRegex);
                                System.out.println("Making request to: " + steamLink);
                                if (steamLink != null) {
                                    Object result = mainPage.evaluate("link => {" +
                                            "  return fetch(link)" +
                                            "    .then(response => response.text())" +
                                            "    .then(data => { return data; });" +
                                            "}", steamLink);

                                    System.out.println("Fetched data: " + result);

                                }
                                System.out.println("Made the request");
                                Email.cancelMailbox(apiKey,id2);
                            }

                            mainPage.waitForTimeout(1000);
                            mainPage.click("xpath=//span[@onclick='handleNextButton()']");

                        } else if (MainUi.is5simChecked()) {
                            mainPage.locator("//*[@id=\"tel_entry\"]").fill(phoneNumber);
                            mainPage.waitForTimeout(1000);
                            mainPage.click("xpath=//span[@onclick='handleNextButton()']");

                            if(MainUi.isEmailsDotTxtSelected()) {
                                PhoneEmail phoneEmail = new PhoneEmail(email1, password1);
                                phoneEmail.processSteamVerificationLink();
                            }else{

                                String id2 = Email.reorderMailbox(email1,apiKey);
                                String linkRegex = "https://store\\.steampowered\\.com/phone/ConfirmEmailForAdd\\?stoken=.*?&steamid=[^\\s\"']++";

                                String steamLink = getSteamLink(apiKey, id2, linkRegex);
                                System.out.println("Making request to: " + steamLink);
                                if (steamLink != null) {
                                    // Use the link to make a GET request
                                    Object result = mainPage.evaluate("link => {" +
                                            "  return fetch(link)" +
                                            "    .then(response => response.text())" +
                                            "    .then(data => { return data; });" +
                                            "}", steamLink);

                                    System.out.println("Fetched data: " + result);

                                }
                                System.out.println("Made the request");
                                Email.cancelMailbox(apiKey,id2);
                            }

                            mainPage.waitForTimeout(1000);
                            mainPage.click("xpath=//span[@onclick='handleNextButton()']");
                        } else if (MainUi.isSmsHubSelected()) {
                            mainPage.locator("//*[@id=\"tel_entry\"]").fill("+" + phoneNumber);
                            mainPage.waitForTimeout(1000);
                            mainPage.click("xpath=//span[@onclick='handleNextButton()']");



                            if(MainUi.isEmailsDotTxtSelected()) {
                                PhoneEmail phoneEmail = new PhoneEmail(email1, password1);
                                phoneEmail.processSteamVerificationLink();
                            }else{
                                String id2 = Email.reorderMailbox(email1,apiKey);
                                String linkRegex = "https://store\\.steampowered\\.com/phone/ConfirmEmailForAdd\\?stoken=.*?&steamid=[^\\s\"']++";

                                String steamLink = getSteamLink(apiKey, id2, linkRegex);
                                System.out.println("Making request to: " + steamLink);
                                if (steamLink != null) {
                                    Object result = mainPage.evaluate("link => {" +
                                            "  return fetch(link)" +
                                            "    .then(response => response.text())" +
                                            "    .then(data => { return data; });" +
                                            "}", steamLink);

                                    System.out.println("Fetched data: " + result);

                                }
                                System.out.println("Made the request");
                                Email.cancelMailbox(apiKey,id2);
                            }





                            mainPage.waitForTimeout(1000);
                            mainPage.click("xpath=//span[@onclick='handleNextButton()']");
                            mainPage.waitForTimeout(2000);
                            if ((MainUi.isSmsHubSelected())) {
                                boolean textExists = (boolean) mainPage.evaluate("document.body.innerText.includes('Unfortunately we are unable to accept VOIP phone numbers. Please enter a mobile number associated with a phone that you physically have in your possession.')");
                                if (textExists) {
                                    String apiUrl = "https://smshub.org/stubs/handler_api.php?api_key=" + smsApiKey + "&action=setStatus&status=8&id=" + id;
                                    URL url = new URL(apiUrl);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");

                                    mainPage.close();
                                    publishProgress("Steam VOIP error, closing browser and canceling number ");
                                } else {
                                    System.out.println("The text was not found on the page.");
                                }

                            }
                        }
                        String sms;
                        if (MainUi.isSmsPvaChecked()) {
                            sms = SmsPvaRequest.getSmsMessage(id, smsApiKey, country);
                        } else if (MainUi.is5simChecked()) {
                            sms = FiveSimRequest.getSMSCode(id, smsApiKey);
                        } else if (MainUi.isSmsHubSelected()) {
                            sms = SmsHubRequest.getSmsCode(id, smsApiKey);
                        } else {
                            throw new Exception("Neither SMS PVA nor 5Sim was selected.");
                        }


                        if (sms == null) {
                            publishProgress("Didn't get sms code in 60 seconds canceling and trying again.");
                            OkHttpClient client = new OkHttpClient();
                            String apiUrl;

                            if (MainUi.isSmsPvaChecked()) {
                                apiUrl = "https://smspva.com/priemnik.php?metod=ban&service=opt78&apikey=" + smsApiKey + "&id=" + id;
                            } else if (MainUi.is5simChecked()) {
                                apiUrl = "https://5sim.net/v1/user/cancel/" + id;
                            } else if (MainUi.isSmsHubSelected()) {
                                apiUrl = "https://smshub.org/stubs/handler_api.php?api_key=" + smsApiKey + "&action=setStatus&status=8&id=" + id;
                            } else {
                                throw new Exception("No valid selection made.");
                            }

                            Request request = new Request.Builder()
                                    .url(apiUrl)
                                    .get()
                                    .build();

                            try (Response response = client.newCall(request).execute()) {
                                System.out.println(response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mainPage.close();
                            createAccounts();
                        } else {
                            mainPage.waitForTimeout(1000);
                            publishProgress("Got the sms code:  " + sms);
                            mainPage.fill("#text_entry", sms);
                            mainPage.waitForTimeout(1000);
                            mainPage.click("xpath=//span[@onclick='handleNextButton()']");
                            mainPage.waitForTimeout(1000);
                            mainPage.click("span[onclick='handleDoneButton()']");
                            mainPage.waitForTimeout(1000);
                            publishProgress("Added the phone number info now closing! ");
                            saveInfo(email1, password2, username4, password1, phoneNumber);
                            return;
                        }
                    } else {
                        saveInfo(email1, password2, username4, password1, "none");
                        mainPage.waitForTimeout(2000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress("An error occurred: " + e.toString());
                }

                mainPage.close();
                context.close();
                Thread.sleep(1000);

//                try {
//                    FileUtils.deleteDirectory(tempProfileDir.toFile());
//                } catch (IOException e) {
//                    throw new RuntimeException("Failed to delete temporary profile directory");
//                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void publishProgress(String message) {
            SwingUtilities.invokeLater(() -> publish(message));
        }
    }
}



