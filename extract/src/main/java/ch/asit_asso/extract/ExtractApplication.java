package ch.asit_asso.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



/**
 * The entry point of this web application.
 *
 * @author Yves Grasset
 */
@SpringBootApplication
public class ExtractApplication extends SpringBootServletInitializer implements WebMvcConfigurer {

    @Override
    protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(ExtractApplication.class);
    }

    /**
     * The writer to the application logs.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractApplication.class);

    /**
     * The localized strings provider.
     */
    @Autowired
    private MessageSource messageSource;



    /**
     * Starts this web application.
     *
     * @param arguments the parameters for this web application
     */
    public static void main(final String[] arguments) {
        ExtractApplication.LOGGER.info("Starting Extract application.");
        SpringApplication.run(ExtractApplication.class, arguments);
    }



    /**
     * Initializes the object that validates the data submitted to this application.
     *
     * @return the validator bean
     */
    @Bean(name = "validator")
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(this.messageSource);

        return validatorFactoryBean;
    }



    /**
     * Obtains the object that validates the data submitted to this application.
     *
     * @return the validator object
     */
    @Override
    public final Validator getValidator() {
        return this.validator();
    }

}
