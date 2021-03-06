package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.ERROR_TOKEN_FACTORY;
import static io.vertx.core.json.impl.JsonUtil.*;

/**
 * Verifies and generates tokens for access.
 */
public class TokenFactory {
    private static final String CRYPTO_TYPE = "type";
    private static final String ALIAS = "alias";
    private final byte[] secret;
    private CoreContext core;

    /**
     * @param core   the core context to run async operations on.
     * @param secret the secret to use to generate HMAC tokens, must not be null.
     */
    public TokenFactory(CoreContext core, byte[] secret) {
        Objects.requireNonNull(secret, "Cannot create TokenFactory with 'null' secret.");
        this.secret = secret;
        this.core = core;
    }

    /**
     * Verifies the validity of the given token.
     *
     * @param token the token to be verified.
     * @return true if the token is accepted.
     */
    public Future<Void> verify(Token token) {
        // verify token not null and token is still valid.
        if (token != null && token.getExpiry() > Instant.now().getEpochSecond()) {
            if (token.getProperties().containsKey(CRYPTO_TYPE)) {
                String algorithm = token.getProperty(CRYPTO_TYPE);
                SecuritySettings security = Configurations.security();

                // don't trust the algorithm in the token - match existing algorithms only.
                if (algorithm.equals(security.getHmacAlgorithm())) {
                    return verifyHmac(token);
                } else if (algorithm.equals(security.getSignatureAlgorithm())) {
                    return verifySignature(token);
                } else {
                    return Future.failedFuture(
                            String.format("Token algorithm '%s' - not enabled/trusted.", algorithm));
                }
                // only log an error if the token is secured and type is missing.
            } else if (token.getKey() != null && !token.getKey().isEmpty()) {
                return Future.failedFuture(
                        String.format("Token is missing property '%s' - unable to verify.", CRYPTO_TYPE));
            }
        }
        return Future.failedFuture("Token is not valid.");
    }

    private Future<Void> verifyHmac(Token token) {
        Promise<Void> promise = Promise.promise();
        core.blocking((blocking) -> {
            try {
                byte[] result = BASE64_ENCODER.encode(hmacKey(token));
                if (ByteComparator.compare(result, token.getKey().getBytes())) {
                    blocking.complete();
                } else {
                    blocking.fail("Failed to verify HMAC token.");
                }
            } catch (Exception e) {
                blocking.fail(e);
            }
        }, promise);
        return promise.future();
    }

    private byte[] hmacKey(Token token) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = Configurations.security().getHmacAlgorithm();
        Mac mac = Mac.getInstance(algorithm);

        SecretKeySpec spec = new SecretKeySpec(secret, algorithm);
        mac.init(spec);
        canonicalizeTokenWithCrypto(token, mac::update);

        return mac.doFinal();
    }

    /**
     * Signs the given token using HMAC.
     *
     * @param token the token to sign, sets the key of this token.
     * @return callback.
     */
    public Future<Void> hmac(Token token) {
        Promise<Void> promise = Promise.promise();
        core.blocking((blocking) -> {
            try {
                token.addProperty(CRYPTO_TYPE, Configurations.security().getHmacAlgorithm());
                token.setKey(BASE64_ENCODER.encodeToString(hmacKey(token)));
                blocking.complete();
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                blocking.fail(ERROR_TOKEN_FACTORY);
            }
        }, promise);
        return promise.future();
    }

    /**
     * Signs the given token using the private key in the named JKS. If the JKS does not
     * exist an error will be thrown.
     *
     * @param token    the token to be signed.
     * @param keystore the keystore that contains the private key to use for signing.
     * @return callback
     */
    public Future<Void> sign(Token token, String keystore) {
        Promise<Void> promise = Promise.promise();
        core.blocking((blocking) -> {
            try {
                byte[] key = signedKey(token, keystore);
                token.setKey(BASE64_ENCODER.encodeToString(key));
                blocking.complete();
            } catch (Throwable e) {
                blocking.fail(e);
            }
        }, promise);
        return promise.future();
    }

    private byte[] signedKey(Token token, String keystore) {
        try {
            TrustAndKeyProvider provider = Configurations.security().getKeystore(keystore);
            Signature signature = Signature.getInstance(Configurations.security().getSignatureAlgorithm());
            signature.initSign(provider.getPrivateKey());

            token.addProperty(CRYPTO_TYPE, Configurations.security().getSignatureAlgorithm());
            token.addProperty(ALIAS, keystore);
            canonicalizeTokenWithCrypto(token, signature::update);

            return signature.sign();
        } catch (Exception e) {
            throw new CoreRuntimeException(e);
        }
    }

    private Future<Void> verifySignature(Token token) {
        Promise<Void> promise = Promise.promise();
        String alias = token.getProperty(ALIAS);

        if (alias == null) {
            promise.fail(String.format("token is missing property '%s' - unable to verify.", ALIAS));
        } else {
            core.blocking((blocking) -> {
                TrustAndKeyProvider provider = Configurations.security().getKeystore(alias);
                try {
                    Signature signature = Signature.getInstance(Configurations.security().getSignatureAlgorithm());
                    signature.initVerify(provider.getPublicKey());
                    canonicalizeTokenWithCrypto(token, signature::update);
                    if (signature.verify(BASE64_DECODER.decode(token.getKey()))) {
                        blocking.complete();
                    } else {
                        blocking.fail("Failed to verify token signature.");
                    }
                } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
                    blocking.fail(e);
                }
            }, promise);
        }
        return promise.future();
    }

    /**
     * Serializes a token and its properties and calls the given crypto function.
     * All data included in the canonicalization is secured.
     *
     * @param token    the token to canonicalize and process with a crypto function.
     * @param function the crypto function to apply to the serialized token parts.
     */
    private void canonicalizeTokenWithCrypto(Token token, CryptoFunction function) {
        try {
            function.update(Serializer.buffer(token.getProperties()).getBytes());
            function.update(token.getDomain().getBytes());
            function.update((token.getExpiry() + "").getBytes());
        } catch (SignatureException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @FunctionalInterface
    private interface CryptoFunction {
        /**
         * processes a data part with a crypto function - this could be a HMAC or signature.
         *
         * @param data the data to be processed.
         */
        void update(byte[] data) throws SignatureException;

    }
}