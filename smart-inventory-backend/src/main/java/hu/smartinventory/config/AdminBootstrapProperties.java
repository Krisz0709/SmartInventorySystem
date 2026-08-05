package hu.smartinventory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bootstrap.admin")
@Getter
@Setter
public class AdminBootstrapProperties {

    private boolean enabled;
    private String username;
    private String password;
    private String email;
    private String fullName;
}