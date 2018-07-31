package fr.tunaki.stackoverflow.burnaki;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("stackexchange.chat")
public class ChatProperties {
    private String email;
    private String password;
    private String botName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }
}
