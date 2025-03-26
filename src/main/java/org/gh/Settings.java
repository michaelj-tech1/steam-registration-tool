package org.gh;
import java.io.Serializable;

public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gmailPassword;
    private String smsApiKey;
    private String country;
    private String numberOfAccounts;
    private String customUsername;
    private String customPassword;
    private String apiKey;
    private String tabsToRun;

    private boolean useProxies;
    private boolean useEmailsTxt;
    private String customCountry;
    private boolean useSmsApi;
    private boolean useSmspva;
    private boolean use5sim;
    private boolean useSmshub;
    private String countryOperator;



    public Settings() {
        this.numberOfAccounts = "";
        this.customUsername = "";
        this.customPassword = "";
        this.apiKey = "";
        this.tabsToRun = "";

        this.useProxies = false;
        this.useEmailsTxt = false;
        this.customCountry = "";
        this.useSmsApi = false;
        this.useSmspva = false;
        this.use5sim = false;
        this.useSmshub = false;
        this.countryOperator = "";
    }

    public String getNumberOfAccounts() {
        return numberOfAccounts;
    }

    public void setNumberOfAccounts(String numberOfAccounts) {
        this.numberOfAccounts = numberOfAccounts;
    }

    public String getCustomUsername() {
        return customUsername;
    }

    public void setCustomUsername(String customUsername) {
        this.customUsername = customUsername;
    }

    public String getCustomPassword() {
        return customPassword;
    }

    public void setCustomPassword(String customPassword) {
        this.customPassword = customPassword;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTabsToRun() {
        return tabsToRun;
    }

    public void setTabsToRun(String tabsToRun) {
        this.tabsToRun = tabsToRun;
    }

    public String getSmsApiKey() {
        return smsApiKey;
    }

    public void setSmsApiKey(String smsApiKey) {
        this.smsApiKey = smsApiKey;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isUseProxies() {
        return useProxies;
    }

    public void setUseProxies(boolean useProxies) {
        this.useProxies = useProxies;
    }

    public boolean isUseEmailsTxt() {
        return useEmailsTxt;
    }

    public void setUseEmailsTxt(boolean useEmailsTxt) {
        this.useEmailsTxt = useEmailsTxt;
    }

    public String getCustomCountry() {
        return customCountry;
    }

    public void setCustomCountry(String customCountry) {
        this.customCountry = customCountry;
    }

    public boolean isUseSmsApi() {
        return useSmsApi;
    }

    public void setUseSmsApi(boolean useSmsApi) {
        this.useSmsApi = useSmsApi;
    }

    public boolean isUseSmspva() {
        return useSmspva;
    }

    public void setUseSmspva(boolean useSmspva) {
        this.useSmspva = useSmspva;
    }

    public boolean isUse5sim() {
        return use5sim;
    }

    public void setUse5sim(boolean use5sim) {
        this.use5sim = use5sim;
    }

    public boolean isUseSmshub() {
        return useSmshub;
    }

    public void setUseSmshub(boolean useSmshub) {
        this.useSmshub = useSmshub;
    }

    public String getCountryOperator() {
        return countryOperator;
    }

    public void setCountryOperator(String countryOperator) {
        this.countryOperator = countryOperator;
    }


    public String getGmailPassword() {
        return gmailPassword;
    }

    public void setGmailPassword(String gmailPassword) {
        this.gmailPassword = gmailPassword;
    }





}
