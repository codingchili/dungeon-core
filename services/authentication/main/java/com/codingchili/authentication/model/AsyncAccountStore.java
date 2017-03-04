package com.codingchili.authentication.model;

import io.vertx.core.Future;

import com.codingchili.core.security.Account;

/**
 * @author Robin Duda
 *         Asynchronous account store.
 */
public interface AsyncAccountStore
{

    /**
     * Finds an account in the store.
     *
     * @param future   callback
     * @param username username of the account to find by username.
     */
    void get(Future<Account> future, String username);

    /**
     * Authenticates an user in the accountstore.
     *
     * @param future  callback
     * @param account unauthenticated account containing username and password.
     */
    void authenticate(Future<Account> future, Account account);

    /**
     * Registers a new account in the store.
     *
     * @param future  callback
     * @param account contains account data to be created.
     */
    void register(Future<Account> future, Account account);
}