package at.phactum.bp.blueprint.modules;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import at.phactum.bp.blueprint.utilities.CaseUtils;

@Configuration(proxyBeanMethods = false)
public class ModuleSpecificPropertiesConfiguration {

    public static final String YAML_EXTENSTION = ".yaml";

    private static final Logger logger = LoggerFactory.getLogger(ModuleSpecificPropertiesConfiguration.class);

    public static class ModuleSpecificProperties {
        private String name;

        public ModuleSpecificProperties(final String name) {
            this.name = name;
        }

        public ModuleSpecificProperties(final String name, final boolean isCamelCase) {
            if (isCamelCase) {
                this.name = CaseUtils.camelToKebap(name);
            } else {
                this.name = name;
            }
        }

        public String getName() {
            return name;
        }
    };

    /**
     * @see https://stackoverflow.com/questions/35197175/spring-what-is-the-programmatic-equivalent-of-propertysource
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer build(final Environment environment,
            final List<ModuleSpecificProperties> modules) {

        final var resources = new LinkedList<Resource>();
        for (final var module : modules) {
            final var defaults = new ClassPathResource("/config/" + module.getName() + YAML_EXTENSTION);
            if (defaults.exists()) {
                logger.debug("Adding yaml-file: {}", defaults.getDescription());
                resources.add(defaults);
            }

            for (final var profile : environment.getActiveProfiles()) {
                final var r = new ClassPathResource("/config/" + module.getName() + "-" + profile + YAML_EXTENSTION);
                if (r.exists()) {
                    logger.debug("Adding yaml-file: {}", r.getDescription());
                    resources.add(r);
                }
            }
        }

        final var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resources.toArray(Resource[]::new));
        yaml.afterPropertiesSet();

        final var ppc = new PropertySourcesPlaceholderConfigurer();
        ppc.setProperties(yaml.getObject());
        ppc.setEnvironment(environment);
        return ppc;

    }

    /**
     * @return The 'module name' which is the first top-level scope.
     */
    public static String getModuleName(final Resource resource) {

        try {

            final var loaderOptions = new LoaderOptions();
            loaderOptions.setAllowDuplicateKeys(false);
            final var yaml = new Yaml(loaderOptions);

            try (final var yamlStream = resource.getInputStream()) {

                Map<String, Object> content = yaml.load(yamlStream);

                return content
                        .entrySet()
                        .stream()
                        .filter(entry -> resource.getFilename().startsWith(entry.getKey()))
                        .findFirst()
                        .map(Entry::getKey)
                        .orElse(null);

            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
