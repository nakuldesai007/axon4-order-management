package com.example.ordermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)
                .addResolver(new ReactResourceResolver());
    }

    private static class ReactResourceResolver implements ResourceResolver {
        private static final String REACT_PATH = "/index.html";

        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath,
                                       List<? extends Resource> locations, ResourceResolverChain chain) {
            // Don't interfere with API, Swagger, H2, or Actuator requests
            if (requestPath.startsWith("/api") || 
                requestPath.startsWith("/swagger-ui") || 
                requestPath.startsWith("/v3/api-docs") ||
                requestPath.startsWith("/h2-console") || 
                requestPath.startsWith("/actuator")) {
                return null;
            }
            
            // Try to resolve the resource normally (for static assets like JS, CSS)
            Resource resource = chain.resolveResource(request, requestPath, locations);
            if (resource != null && resource.exists()) {
                return resource;
            }
            
            // For non-API paths that don't exist, return index.html for React Router
            return chain.resolveResource(request, REACT_PATH, locations);
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                   ResourceResolverChain chain) {
            return chain.resolveUrlPath(resourcePath, locations);
        }
    }
}

