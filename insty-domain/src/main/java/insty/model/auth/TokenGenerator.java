package insty.model.auth;

public interface TokenGenerator {

    String generate(int length);
}
