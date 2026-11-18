package com.mycompany.quickchat;

import java.util.ArrayList;
import java.util.List;

/**
 * Part 3: keeps the arrays of messages and lets the user search, delete and
 * report on them.
 *
 * Five arrays are kept (no hard-coded data, they are filled while the program
 * runs or from the JSON file):
 * sent messages, disregarded messages, stored messages, message hashes and
 * message IDs.
 */
public class MessageManager {

    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> disregardedMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();
    private final List<String> messageHashes = new ArrayList<>();
    private final List<String> messageIds = new ArrayList<>();

    // ---------- Populating the arrays ----------

    /**
     * Adds a message to the correct array using its flag
     * (Message.SEND, Message.DISREGARD or Message.STORE). The hash and ID
     * arrays are updated for every message.
     */
    public void addMessage(Message message, int flag) {

        if (flag == Message.SEND) {
            sentMessages.add(message);
        } else if (flag == Message.DISREGARD) {
            disregardedMessages.add(message);
        } else if (flag == Message.STORE) {
            storedMessages.add(message);
        } else {
            return; // unknown flag, nothing to record
        }

        messageHashes.add(message.getMessageHash());
        messageIds.add(message.getMessageId());
    }

    /**
     * Reads the stored messages from the JSON file into the stored array.
     * Messages that are already in the array (same ID) are not added twice.
     * Returns how many messages were loaded.
     */
    public int loadStoredMessages(String storageFile) {

        int loaded = 0;

        for (Message message : Message.readStoredMessages(storageFile)) {
            if (!messageIds.contains(message.getMessageId())) {
                addMessage(message, Message.STORE);
                loaded++;
            }
        }

        return loaded;
    }

    /** Saves the stored array back to the JSON file (used after a delete). */
    public void saveStoredMessages(String storageFile) {
        Message.writeStoredMessages(storedMessages, storageFile);
    }

    // ---------- Reading the arrays ----------

    public List<String> getSentMessageTexts() {
        return textsOf(sentMessages);
    }

    public List<String> getDisregardedMessageTexts() {
        return textsOf(disregardedMessages);
    }

    public List<String> getStoredMessageTexts() {
        return textsOf(storedMessages);
    }

    public List<String> getMessageHashes() {
        return new ArrayList<>(messageHashes);
    }

    public List<String> getMessageIds() {
        return new ArrayList<>(messageIds);
    }

    private List<String> textsOf(List<Message> messages) {

        List<String> texts = new ArrayList<>();

        for (Message message : messages) {
            texts.add(message.getText());
        }

        return texts;
    }

    // ---------- Features ----------

    /** a) The sender and recipient of every stored message. */
    public String displaySenderAndRecipient() {

        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }

        StringBuilder result = new StringBuilder();

        for (Message message : storedMessages) {
            result.append("Sender: ").append(message.getSender())
                    .append(" | Recipient: ").append(message.getRecipient())
                    .append("\n");
        }

        return result.toString();
    }

    /** b) The longest stored message (the first one if two are equally long). */
    public String getLongestStoredMessage() {

        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }

        Message longest = storedMessages.get(0);

        for (Message message : storedMessages) {
            if (message.getText().length() > longest.getText().length()) {
                longest = message;
            }
        }

        return longest.getText();
    }

    /**
     * c) Finds a message by its ID. Looks through the sent, stored and
     * disregarded arrays. Returns null if the ID is not found.
     */
    public Message findMessageById(String messageId) {

        for (Message message : allMessages()) {
            if (message.getMessageId().equals(messageId)) {
                return message;
            }
        }

        return null;
    }

    /** c) The text shown to the user when they search for a message ID. */
    public String searchByMessageId(String messageId) {

        Message found = findMessageById(messageId);

        if (found == null) {
            return "No message found with ID " + messageId + ".";
        }

        return "Recipient: " + found.getRecipient() + "\nMessage: " + found.getText();
    }

    /** d) The text of every sent or stored message for one recipient. */
    public List<String> searchByRecipient(String recipient) {

        List<String> found = new ArrayList<>();

        for (Message message : sentMessages) {
            if (message.getRecipient().equals(recipient)) {
                found.add(message.getText());
            }
        }

        for (Message message : storedMessages) {
            if (message.getRecipient().equals(recipient)) {
                found.add(message.getText());
            }
        }

        return found;
    }

    /**
     * e) Deletes a message using its hash. The message is removed from every
     * array it is in (sent, disregarded or stored, and the hash and ID arrays).
     */
    public String deleteByHash(String messageHash) {

        Message target = null;

        for (Message message : allMessages()) {
            if (message.getMessageHash().equals(messageHash)) {
                target = message;
                break;
            }
        }

        if (target == null) {
            return "No message found with hash " + messageHash + ".";
        }

        sentMessages.remove(target);
        disregardedMessages.remove(target);
        storedMessages.remove(target);
        messageHashes.remove(target.getMessageHash());
        messageIds.remove(target.getMessageId());

        return "Message: \"" + target.getText() + "\" successfully deleted.";
    }

    /** f) A report with the hash, recipient and message of every sent message. */
    public String displaySentReport() {
        return buildReport("SENT MESSAGES REPORT", sentMessages);
    }

    /** f) A report with the full details of every stored message. */
    public String displayStoredReport() {
        return buildReport("STORED MESSAGES REPORT", storedMessages);
    }

    private String buildReport(String title, List<Message> messages) {

        StringBuilder report = new StringBuilder();
        report.append("===== ").append(title).append(" =====\n");

        if (messages.isEmpty()) {
            report.append("No messages.\n");
            return report.toString();
        }

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            report.append("Message ").append(i + 1).append("\n");
            report.append("Message ID: ").append(message.getMessageId()).append("\n");
            report.append("Message Hash: ").append(message.getMessageHash()).append("\n");
            report.append("Sender: ").append(message.getSender()).append("\n");
            report.append("Recipient: ").append(message.getRecipient()).append("\n");
            report.append("Message: ").append(message.getText()).append("\n\n");
        }

        return report.toString();
    }

    /** Every message in the three arrays, used by the search and delete features. */
    private List<Message> allMessages() {

        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        all.addAll(disregardedMessages);

        return all;
    }
}
