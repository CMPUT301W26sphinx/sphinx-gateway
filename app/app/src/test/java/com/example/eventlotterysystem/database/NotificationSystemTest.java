package com.example.eventlotterysystem.database;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link NotificationSystem}.
 * https://stackoverflow.com/questions/53473006/unit-testing-with-mockito-firebase
 * https://www.geeksforgeeks.org/java/introduction-to-mockito/
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationSystemTest {

// Mocks
    @Mock private ProfileManager mockProfileManager;
    @Mock private FirebaseFirestore mockFirestore;
    @Mock private CollectionReference mockUsersCollection;
    @Mock private CollectionReference mockLogsCollection;
    @Mock private DocumentReference mockDocument;
    @Mock private UserProfile mockUser;

    private NotificationSystem notificationSystem;

    //STUBS - defines what a mock object should return when a method is called.
    private void stubUser(String userId, boolean enabled) {
        when(mockUser.getNotificationPreference()).thenReturn(enabled);
        doAnswer(inv -> {
            ProfileManager.UserProfileCallBack cb = inv.getArgument(1);
            cb.onComplete(mockUser);
            return null;
        }).when(mockProfileManager).getUserProfileById(eq(userId), any());
    }

    //SETUP - mockfirestore and all.
    @Before
    public void setUp() {
        // Stub Firestore chains
        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);
        when(mockFirestore.collection("logs_notification")).thenReturn(mockLogsCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.update(anyString(), any())).thenReturn(null);
        when(mockLogsCollection.add(any())).thenReturn(null);

        try (MockedStatic<ProfileManager> pmStatic = mockStatic(ProfileManager.class)) {
            pmStatic.when(ProfileManager::getInstance).thenReturn(mockProfileManager);
            notificationSystem = new NotificationSystem();
        }
    }

    //Tests start here
    //Main notification system.
    @Test
    public void sendNotification_userEnabled_writesToFirestore() {
        stubUser("user1", true);

        try (MockedStatic<FirebaseFirestore> fsStatic = mockStatic(FirebaseFirestore.class)) {
            fsStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            notificationSystem.sendNotification("user1", "You were selected", "event42", "org1");

            verify(mockUsersCollection).document("user1");
            verify(mockDocument).update(eq("notification"), any(FieldValue.class));
        }
    }

    @Test
    public void sendNotification_userDisabled_doesNotWriteToFirestore() {
        stubUser("user2", false);

        try (MockedStatic<FirebaseFirestore> fsStatic = mockStatic(FirebaseFirestore.class)) {
            fsStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            notificationSystem.sendNotification("user2", "Lottery result", "event99", "org2");

            verify(mockFirestore, never()).collection(anyString());
        }
    }

    // getNotifications

    @Test
    public void getNotifications_returnsMessagesInReverseOrder() {
        String userId = "userA";
        List<String> stored = Arrays.asList("first|e1|org", "second|e2|org", "third|e3|org");
        when(mockUser.getNotification()).thenReturn(stored);
        doAnswer(inv -> {
            ProfileManager.UserProfileCallBack cb = inv.getArgument(1);
            cb.onComplete(mockUser);
            return null;
        }).when(mockProfileManager).getUserProfileById(eq(userId), any());

        AtomicReference<List<String>> result = new AtomicReference<>();
        notificationSystem.getNotifications(userId, result::set);

        List<String> notifications = result.get();
        assertEquals(3, notifications.size());
        assertEquals("third|e3|org",  notifications.get(0)); // newest first
        assertEquals("second|e2|org", notifications.get(1));
        assertEquals("first|e1|org",  notifications.get(2)); // oldest last
    }

    @Test
    public void getNotifications_singleMessage_returnsSingleItemList() {
        String userId = "userD";
        when(mockUser.getNotification()).thenReturn(Collections.singletonList("only|e1|org"));
        doAnswer(inv -> {
            ProfileManager.UserProfileCallBack cb = inv.getArgument(1);
            cb.onComplete(mockUser);
            return null;
        }).when(mockProfileManager).getUserProfileById(eq(userId), any());

        AtomicReference<List<String>> result = new AtomicReference<>();
        notificationSystem.getNotifications(userId, result::set);

        assertEquals(1, result.get().size());
        assertEquals("only|e1|org", result.get().get(0));
    }

    @Test
    public void getNotifications_emptyList_returnsEmptyList() {
        String userId = "userB";
        when(mockUser.getNotification()).thenReturn(Collections.emptyList());
        doAnswer(inv -> {
            ProfileManager.UserProfileCallBack cb = inv.getArgument(1);
            cb.onComplete(mockUser);
            return null;
        }).when(mockProfileManager).getUserProfileById(eq(userId), any());

        AtomicReference<List<String>> result = new AtomicReference<>();
        notificationSystem.getNotifications(userId, result::set);

        assertTrue(result.get().isEmpty());
    }

    // deleteNotification

    @Test
    public void deleteNotification_callsArrayRemoveOnCorrectDocument() {
        try (MockedStatic<FirebaseFirestore> fsStatic = mockStatic(FirebaseFirestore.class)) {
            fsStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            notificationSystem.deleteNotification("userDel", "msg|event1|org");

            verify(mockUsersCollection).document("userDel");
            verify(mockDocument).update(eq("notification"), any(FieldValue.class));
        }
    }

    @Test
    public void deleteNotification_calledForTwoUsers_targetsCorrectDocuments() {
        try (MockedStatic<FirebaseFirestore> fsStatic = mockStatic(FirebaseFirestore.class)) {
            fsStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            notificationSystem.deleteNotification("user_x", "msgA|e1|org");
            notificationSystem.deleteNotification("user_y", "msgB|e2|org");

            verify(mockUsersCollection).document("user_x");
            verify(mockUsersCollection).document("user_y");
            verify(mockDocument, times(2)).update(eq("notification"), any(FieldValue.class));
        }
    }

    // logNotification
    // Used claudeai on this one, way too confusing.
    // prompt: can you create a test for lognotification.

    @Test
    public void logNotification_writesAllRequiredFieldsToLogsCollection() {
        try (MockedStatic<FirebaseFirestore> fsStatic = mockStatic(FirebaseFirestore.class);
             MockedStatic<ProfileManager> pmStatic = mockStatic(ProfileManager.class)) {

            fsStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
            pmStatic.when(ProfileManager::getInstance).thenReturn(mockProfileManager);
            when(mockProfileManager.getUserID()).thenReturn("organizer99");

            notificationSystem.logNotification("event77", "Lottery drawn");

            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(mockLogsCollection).add(mapCaptor.capture());

            Map logged = mapCaptor.getValue();
            assertEquals("organizer99",   logged.get("OrganizerID"));
            assertEquals("event77",       logged.get("EventID"));
            assertEquals("Lottery drawn", logged.get("Message"));
            assertNotNull(logged.get("Time")); // Timestamp present
        }
    }

    @Test
    public void logNotification_usesCurrentOrganizerIdFromProfileManager() {
        try (MockedStatic<FirebaseFirestore> fsStatic = mockStatic(FirebaseFirestore.class);
             MockedStatic<ProfileManager> pmStatic = mockStatic(ProfileManager.class)) {

            fsStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
            pmStatic.when(ProfileManager::getInstance).thenReturn(mockProfileManager);
            when(mockProfileManager.getUserID()).thenReturn("anotherOrg");

            notificationSystem.logNotification("eventX", "Test message");

            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(mockLogsCollection).add(mapCaptor.capture());
            assertEquals("anotherOrg", mapCaptor.getValue().get("OrganizerID"));
        }
    }
}