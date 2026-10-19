package futabus.gateway.configuration.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "futa.gateway")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FutaGatewayProperties {
    List<String> blockedPaths = new ArrayList<>();
}
