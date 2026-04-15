package tech.exitzero.blog.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "blog.storage")
public class StorageProperties {

    private String location = "./uploads";
    private String publicPath = "/uploads";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPublicPath() {
        return publicPath;
    }

    public void setPublicPath(String publicPath) {
        this.publicPath = publicPath;
    }

    public Path resolveLocation() {
        return Paths.get(location).toAbsolutePath().normalize();
    }
}
