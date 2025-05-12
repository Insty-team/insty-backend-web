package insty.pg.error;

import insty.error.ErrorCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ExampleTossErrorCode implements ErrorCode {
    EXAMPLE_TOSS_API("", "", 0);

    private final String code;
    private final String message;
    private final int httpCode;

    @Override
    public String getCode() {
        return "";
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public int getHttpCode() {
        return 0;
    }
}
