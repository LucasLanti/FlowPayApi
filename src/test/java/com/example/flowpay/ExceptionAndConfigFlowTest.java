package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.example.flowpay.configs.AppConfig;
import com.example.flowpay.configs.OpenApiConfig;
import com.example.flowpay.configs.RabbitMqConfig;
import com.example.flowpay.configs.Translator;
import com.example.flowpay.configs.WebConfigs;
import com.example.flowpay.exceptions.BadRequestException;
import com.example.flowpay.exceptions.GlobalExceptionHandler;
import com.example.flowpay.exceptions.NotFoundException;

class ExceptionAndConfigFlowTest {

    @Test
    void exceptionHandlerReturnsExpectedResponses() throws Exception {
        Translator translator = mock(Translator.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(translator);
        when(translator.translate(eq("not.found"), any(), any())).thenReturn("Nao encontrado");
        when(translator.translate(eq("bad.request"), any(), any())).thenReturn("Requisicao invalida");
        when(translator.translate(eq("request.body.invalid"), any(), any())).thenReturn("Body invalido");
        when(translator.translate(eq("request.parameter.invalid"), any(), any())).thenReturn("Parametro invalido");
        when(translator.translate(eq("request.validation.invalid"), any(), any())).thenReturn("Validacao invalida");
        when(translator.translate(eq("endpoint.not_found"), any(), any())).thenReturn("Endpoint nao encontrado");
        when(translator.translate(eq("error.internal"), any(), any())).thenReturn("Erro interno");

        TestFixtures.assertError(handler.handleNotFound(new NotFoundException("fallback", "not.found"), null),
                HttpStatus.NOT_FOUND, "not.found", "Nao encontrado");
        TestFixtures.assertError(handler.handleBadRequest(new BadRequestException("fallback", "bad.request"), null),
                HttpStatus.BAD_REQUEST, "bad.request", "Requisicao invalida");

        Object target = new Object();
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(target, "target");
        binding.addError(new FieldError("target", "field", "Campo obrigatorio"));
        MethodParameter parameter = new MethodParameter(
                ExceptionAndConfigFlowTest.class.getDeclaredMethod("dummyValidationTarget", String.class), 0);
        MethodArgumentNotValidException validation = new MethodArgumentNotValidException(parameter, binding);
        TestFixtures.assertError(handler.handleValidation(validation, null), HttpStatus.BAD_REQUEST,
                "request.validation.invalid", "Campo obrigatorio");

        BeanPropertyBindingResult emptyBinding = new BeanPropertyBindingResult(target, "target");
        MethodArgumentNotValidException emptyValidation = new MethodArgumentNotValidException(parameter, emptyBinding);
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleValidation(emptyValidation, null).getStatusCode());

        BeanPropertyBindingResult nullMessageBinding = new BeanPropertyBindingResult(target, "target");
        nullMessageBinding.addError(new FieldError("target", "field", null));
        MethodArgumentNotValidException nullMessageValidation = new MethodArgumentNotValidException(parameter,
                nullMessageBinding);
        TestFixtures.assertError(handler.handleValidation(nullMessageValidation, null), HttpStatus.BAD_REQUEST,
                "request.validation.invalid", "Validacao invalida");

        TestFixtures.assertError(handler.handleInvalidBody(new HttpMessageNotReadableException("bad",
                new MockHttpInputMessage(new byte[0])), null), HttpStatus.BAD_REQUEST, "request.body.invalid",
                "Body invalido");
        MethodArgumentTypeMismatchException mismatch = new MethodArgumentTypeMismatchException("x", UUID.class, "id",
                null, new IllegalArgumentException("bad"));
        TestFixtures.assertError(handler.handleInvalidParameter(mismatch, null), HttpStatus.BAD_REQUEST,
                "request.parameter.invalid", "Parametro invalido");
        TestFixtures.assertError(handler.handleEndpointNotFound(new RuntimeException("missing"), null),
                HttpStatus.NOT_FOUND, "endpoint.not_found", "Endpoint nao encontrado");
        TestFixtures.assertError(handler.handleGeneric(new RuntimeException("boom"), null),
                HttpStatus.INTERNAL_SERVER_ERROR, "error.internal", "Erro interno");
    }

    @Test
    void configsAndTranslatorCreateExpectedBeans() {
        MessageSource source = mock(MessageSource.class);
        when(source.getMessage(eq("key"), any(), eq("fallback"), eq(Locale.of("pt", "BR")))).thenReturn("traduzido");
        Translator translator = new Translator(source);
        assertEquals("traduzido", translator.translate("key", null, "fallback"));
        when(source.getMessage(eq("same"), any(), eq("same"), eq(Locale.of("pt", "BR")))).thenReturn("same");
        assertEquals("same", translator.translate("same"));
        assertThrows(NullPointerException.class, () -> new Translator(null));

        AppConfig appConfig = new AppConfig();
        appConfig.setDefaultLocale();
        assertEquals(Locale.of("pt", "BR"), Locale.getDefault());
        assertInstanceOf(RestTemplate.class, appConfig.restTemplate());
        ReloadableResourceBundleMessageSource messageSource = appConfig.messageSource();
        LocalValidatorFactoryBean validator = appConfig.validator(messageSource);
        assertNotNull(validator);

        WebConfigs webConfigs = new WebConfigs(validator);
        assertSame(validator, webConfigs.getValidator());
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);
        when(registry.addMapping("/**")).thenReturn(registration);
        webConfigs.addCorsMappings(registry);
        verify(registry).addMapping("/**");

        OpenApiConfig openApiConfig = new OpenApiConfig();
        assertEquals("FlowPay API", openApiConfig.flowPayOpenAPI().getInfo().getTitle());
        assertEquals("v1", openApiConfig.flowPayOpenAPI().getInfo().getVersion());
    }

    @Test
    void rabbitConfigCreatesQueuesBindingsAndTemplates() {
        RabbitMqConfig config = new RabbitMqConfig();
        ReflectionTestUtils.setField(config, "ticketExchange", "exchange");
        ReflectionTestUtils.setField(config, "cardsQueue", "cards.queue");
        ReflectionTestUtils.setField(config, "cardsRoutingKey", "cards.key");
        ReflectionTestUtils.setField(config, "borrowQueue", "borrow.queue");
        ReflectionTestUtils.setField(config, "borrowRoutingKey", "borrow.key");
        ReflectionTestUtils.setField(config, "othersQueue", "others.queue");
        ReflectionTestUtils.setField(config, "othersRoutingKey", "others.key");

        DirectExchange exchange = config.ticketExchange();
        Queue cards = config.cardsQueue();
        Queue borrow = config.borrowQueue();
        Queue others = config.othersQueue();
        Binding cardsBinding = config.cardsBinding(cards, exchange);
        Binding borrowBinding = config.borrowBinding(borrow, exchange);
        Binding othersBinding = config.othersBinding(others, exchange);
        MessageConverter converter = config.jsonMessageConverter();
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        RabbitAdmin admin = config.rabbitAdmin(connectionFactory);
        RabbitTemplate template = config.rabbitTemplate(connectionFactory, converter);

        assertEquals("exchange", exchange.getName());
        assertEquals("cards.queue", cards.getName());
        assertEquals("borrow.queue", borrow.getName());
        assertEquals("others.queue", others.getName());
        assertEquals("cards.key", cardsBinding.getRoutingKey());
        assertEquals("borrow.key", borrowBinding.getRoutingKey());
        assertEquals("others.key", othersBinding.getRoutingKey());
        assertNotNull(converter);
        assertNotNull(admin);
        assertNotNull(template);
    }

    @Test
    void applicationMainDelegatesToSpringApplication() {
        String[] args = { "test" };
        assertNotNull(new FlowPayApplication());

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            FlowPayApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(FlowPayApplication.class, args));
        }
    }

    @SuppressWarnings("unused")
    private void dummyValidationTarget(String value) {
    }
}
