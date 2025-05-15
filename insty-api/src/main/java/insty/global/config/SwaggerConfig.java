package insty.global.config;

import static java.util.stream.Collectors.groupingBy;

import insty.error.ErrorCode;
import insty.global.advice.ParameterData;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.error.CommonErrorCode;
import insty.global.response.ErrorInfo;
import insty.global.response.FailRes;
import insty.global.swagger.ExampleHolder;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@OpenAPIDefinition(
        info = @Info(
                title = "인스티 백엔드 API 명세서",
                description = "springdoc을 이용한 Swagger API 문서입니다.",
                version = "1.0"
        )
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            CustomExceptionDescription customExceptionDescription = handlerMethod.getMethodAnnotation(
                    CustomExceptionDescription.class);

            // CustomExceptionDescription 어노테이션을 단 메소드에만 적용
            if (customExceptionDescription != null) {
                generateErrorCodeResponseExample(operation, customExceptionDescription.value());
            }

            return operation;
        };
    }

    private void generateErrorCodeResponseExample(Operation operation, SwaggerResponseDescription type) {
        ApiResponses responses = operation.getResponses();

        Set<ErrorCode> errorCodeList = type.getErrorCodeList();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders =
                errorCodeList.stream()
                        .map(
                                errorCode ->
                                        ExampleHolder.builder()
                                                .holder(
                                                        getSwaggerExample(errorCode))
                                                .code(errorCode.getHttpCode())
                                                .name(errorCode.toString())
                                                .build()
                        )
                        .collect(groupingBy(ExampleHolder::getCode));
        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private Example getSwaggerExample(ErrorCode errorCode) {
        Object detail = null;

        if (Objects.equals(errorCode.getCode(), CommonErrorCode.PARAMETER_VALIDATION_ERROR.getCode())) {
            Map<String, String> result = new LinkedHashMap<>();

            result.put("key", "검증 대상 파라미터");
            result.put("value", "받은 파라미터 값");
            result.put("reason", "검증 에러 원인 메세지");

            ParameterData parameterData = new ParameterData("검증 대상 파라미터", "받은 파라미터 값", "검증 에러 원인 메세지");
            detail = List.of(parameterData);
        }

        FailRes<Object> mapFailRes = FailRes.of(ErrorInfo.ofWithDetails(errorCode, detail));

        Example example = new Example();
        example.description(errorCode.getMessage());
        example.setValue(mapFailRes);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses,
                                        Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach(
                (status, v) -> {
                    Content content = new Content();
                    MediaType mediaType = new MediaType();
                    ApiResponse apiResponse = new ApiResponse();
                    v.forEach(
                            exampleHolder ->
                                    mediaType.addExamples(exampleHolder.getName(), exampleHolder.getHolder()));
                    content.addMediaType("application/json", mediaType);
                    apiResponse.setDescription("");
                    apiResponse.setContent(content);
                    responses.addApiResponse(status.toString(), apiResponse);
                });
    }
}
