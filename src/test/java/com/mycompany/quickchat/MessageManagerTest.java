package com.mycompany.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 3 unit tests. The arrays are filled with the five test messages from
 * the task, using the flag given for each one (Sent, Stored, Disregard).
 */
public class MessageManagerTest {

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    private MessageManager manager;
    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {

        manager = new MessageManager();

        // The task gives message 4 the ID 0838884567; the others use fixed IDs
        // so that the hashes are known in advance.
        message1 = new Message("1000000001", 0, "+27834557896", TEXT_1, "Developer");
        message2 = new Message("2000000002", 1, "+27838884567", TEXT_2, "Developer");
        message3 = new Message("3000000003", 2, "+27834484567", TEXT_3, "Developer");
        message4 = new Message("0838884567", 3, "0838884567", TEXT_4, "Developer");
        message5 = new Message("5000000005", 4, "+27838884567", TEXT_5, "Developer");

        manager.addMessage(message1, Message.SEND);
        manager.addMessage(message2, Message.STORE);
        manager.addMessage(message3, Message.DISREGARD);
        manager.addMessage(message4, Message.SEND);
        manager.addMessage(message5, Message.STORE);
    }

    // ---------- Arrays correctly populated ----------

    @Test
    public void testSentMessagesArray_Populated() {
        assertEquals(Arrays.asList(TEXT_1, TEXT_4), manager.getSentMessageTexts());
    }

    @Test
    public void testDisregardedMessagesArray_Populated() {
        assertEquals(Arrays.asList(TEXT_3), manager.getDisregardedMessageTexts());
    }

    @Test
    public void testStoredMessagesArray_Populated() {
        assertEquals(Arrays.asList(TEXT_2, TEXT_5), manager.getStoredMessageTexts());
    }

    @Test
    public void testMessageHashArray_Populated() {
        List<String> expected = Arrays.asList(
                "10:0:DIDCAKE", "20:1:WHERETIME", "30:2:YOHOOOOGATE", "08:3:ITTIME", "50:4:OKYOU");
        assertEquals(expected.size(), manager.getMessageHashes().size());
        assertTrue(manager.getMessageHashes().containsAll(expected));
    }

    @Test
    public void testMessageIdArray_Populated() {
        assertEquals(5, manager.getMessageIds().size());
        assertTrue(manager.getMessageIds().contains("0838884567"));
    }

    // ---------- Longest message ----------

    @Test
    public void testLongestMessage() {
        assertEquals(TEXT_2, manager.getLongestStoredMessage());
    }

    @Test
    public void testLongestMessage_NoStoredMessages() {
        assertEquals("There are no stored messages.", new MessageManager().getLongestStoredMessage());
    }

    // ---------- Search for a message ID ----------

    @Test
    public void testSearchByMessageId_Message4() {
        Message found = manager.findMessageById("0838884567");
        assertNotNull(found);
        assertEquals(TEXT_4, found.getText());
    }

    @Test
    public void testSearchByMessageId_ShowsRecipientAndMessage() {
        assertEquals("Recipient: 0838884567\nMessage: " + TEXT_4,
                manager.searchByMessageId("0838884567"));
    }

    @Test
    public void testSearchByMessageId_NotFound() {
        assertNull(manager.findMessageById("9999999999"));
        assertEquals("No message found with ID 9999999999.", manager.searchByMessageId("9999999999"));
    }

    // ---------- Search by recipient ----------

    @Test
    public void testSearchByRecipient() {
        assertEquals(Arrays.asList(TEXT_2, TEXT_5), manager.searchByRecipient("+27838884567"));
    }

    @Test
    public void testSearchByRecipient_NoMessages() {
        assertTrue(manager.searchByRecipient("+27000000000").isEmpty());
    }

    // ---------- Sender and recipient ----------

    @Test
    public void testDisplaySenderAndRecipient() {
        String expected = "Sender: Developer | Recipient: +27838884567\n"
                + "Sender: Developer | Recipient: +27838884567\n";
        assertEquals(expected, manager.displaySenderAndRecipient());
    }

    // ---------- Delete using the message hash ----------

    @Test
    public void testDeleteByHash_Message2() {
        String result = manager.deleteByHash(message2.getMessageHash());

        assertEquals("Message: \"" + TEXT_2 + "\" successfully deleted.", result);
        // it is gone from the arrays
        assertEquals(Arrays.asList(TEXT_5), manager.getStoredMessageTexts());
        assertFalse(manager.getMessageHashes().contains("20:1:WHERETIME"));
        assertFalse(manager.getMessageIds().contains("2000000002"));
    }

    @Test
    public void testDeleteByHash_NotFound() {
        assertEquals("No message found with hash 00:0:NOPE.", manager.deleteByHash("00:0:NOPE"));
        assertEquals(2, manager.getStoredMessageTexts().size());
    }

    // ---------- Report ----------

    @Test
    public void testDisplaySentReport() {
        String report = manager.displaySentReport();

        assertTrue(report.contains("Message Hash: 10:0:DIDCAKE"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: " + TEXT_1));
        assertTrue(report.contains("Message Hash: 08:3:ITTIME"));
        assertTrue(report.contains("Message: " + TEXT_4));
        // stored and disregarded messages are not in the sent report
        assertFalse(report.contains(TEXT_2));
        assertFalse(report.contains(TEXT_3));
    }

    @Test
    public void testDisplayStoredReport() {
        String report = manager.displayStoredReport();

        assertTrue(report.contains("Message ID: 2000000002"));
        assertTrue(report.contains("Message Hash: 20:1:WHERETIME"));
        assertTrue(report.contains("Recipient: +27838884567"));
        assertTrue(report.contains("Message: " + TEXT_5));
        assertFalse(report.contains(TEXT_1));
    }

    @Test
    public void testDisplayReport_NoMessages() {
        assertTrue(new MessageManager().displaySentReport().contains("No messages."));
    }

    // ---------- JSON file read into the stored array ----------

    @Test
    public void testLoadStoredMessages_ReadsJsonIntoArray() {
        String file = tempDir.resolve("stored.json").toString();
        message2.storeMessage(file);
        message5.storeMessage(file);

        MessageManager fresh = new MessageManager();
        int loaded = fresh.loadStoredMessages(file);

        assertEquals(2, loaded);
        assertEquals(Arrays.asList(TEXT_2, TEXT_5), fresh.getStoredMessageTexts());
        assertEquals(2, fresh.getMessageHashes().size());
        assertEquals(2, fresh.getMessageIds().size());
    }

    @Test
    public void testLoadStoredMessages_DoesNotAddTheSameMessageTwice() {
        String file = tempDir.resolve("stored.json").toString();
        message2.storeMessage(file);

        MessageManager fresh = new MessageManager();
        fresh.loadStoredMessages(file);
        fresh.loadStoredMessages(file);

        assertEquals(1, fresh.getStoredMessageTexts().size());
    }

    @Test
    public void testSaveStoredMessages_AfterDelete() {
        String file = tempDir.resolve("stored.json").toString();
        message2.storeMessage(file);
        message5.storeMessage(file);

        MessageManager fresh = new MessageManager();
        fresh.loadStoredMessages(file);
        fresh.deleteByHash(message2.getMessageHash());
        fresh.saveStoredMessages(file);

        List<Message> inFile = Message.readStoredMessages(file);
        assertEquals(1, inFile.size());
        assertEquals(TEXT_5, inFile.get(0).getText());
    }
}
