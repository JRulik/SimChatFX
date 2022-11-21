package com.simchat.shared.dataclasses;

/**
 * Types of message used in Message class. Used on both sides (server-client) to filter incoming messages by their meaning
 * (e.g. standard message between users or some special message (e.g. client requesting return friendlist of user))
 */
public enum MessageType {
    LOGIN_MESSAGE, SIGNUP_MESSAGE, STANDARD_MESSAGE, ADD_FRIEND, RETURN_FRIENDLIST, RETURN_MESSAGES_BETWEEN_USERS
}
