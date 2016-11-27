package com.codingchili.core.security;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.configuration.system.ValidatorSettings;
import com.codingchili.core.protocol.exception.RequestValidationException;
import com.codingchili.core.files.Configurations;

import static com.codingchili.core.configuration.Strings.*;

/**
 * @author Robin Duda
 *
 * Tests the validation mechanism.
 */
@RunWith(VertxUnitRunner.class)
public class ValidatorTest {
    private static Validator validator = new Validator();
    private static ValidatorSettings settings;

    @Before
    public void setUp() {
        settings = Configurations.validator();
    }

    @Test
    public void testMessageIsFiltered() throws RequestValidationException {
        Assert.assertEquals("test " +
                settings.getValidators().get("chat-messages").regex.get(0).replacement +
                " hello", getMessage("test f**k hello"));
    }

    @Test
    public void testMessageNotFiltered() throws RequestValidationException {
        Assert.assertEquals("hello test meow", getMessage("hello test meow"));
    }

    @Test
    public void testUsernameFilterRejected() {
        try {
            getUser("invalid string with space.. #@!");
            throw new RuntimeException("Validation error to detect malicious input.");
        } catch (RequestValidationException ignored) {
        }
    }

    @Test
    public void testPlainText(TestContext test) {
        test.assertTrue(plaintext("abc102"));
        test.assertTrue(plaintext("abc 102"));
        test.assertTrue(plaintext("abc-102"));
        test.assertTrue(plaintext(1000));
        test.assertTrue(plaintext(1000L));
        test.assertTrue(plaintext(new Byte("0")));

        String[] invalid = {"?", "_", "%", "^", ".", "*"};

        for (String character : invalid) {
            test.assertFalse(plaintext(character));
        }
    }

    private boolean plaintext(Comparable comparable) {
        return validator.plainText(comparable);
    }

    @Test
    public void testUsernameValidated() throws RequestValidationException {
        getUser("myFirstUserName");
        getUser("anotherFantasticUsername");
        getUser("000TheUserName123");
    }

    @Test
    public void testMinimumLengthIsEnforced() {
        String tooShort = "i";

        try {
            getUser(tooShort);
            throw new RuntimeException("Too short string does not fail to validate.");
        } catch (RequestValidationException ignored) {
        }
    }

    @Test
    public void testMaximumLengthIsEnforced() {
        String tooLong = new String(new char[100]).replace("\0", "X");

        try {
            getMessage(tooLong);
            throw new RuntimeException("Too long string does not fail to validate.");
        } catch (RequestValidationException ignored) {
        }
    }

    private String getMessage(String message) throws RequestValidationException {
        return validate(new JsonObject().put(ID_MESSAGE, message)).getString(ID_MESSAGE);
    }

    private String getUser(String user) throws RequestValidationException {
        return validate(new JsonObject().put(ID_NAME, user)).getString(ID_NAME);
    }

    private JsonObject validate(JsonObject json) throws RequestValidationException {
        return validator.validate(json);
    }
}
