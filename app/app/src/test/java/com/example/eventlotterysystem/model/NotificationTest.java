package com.example.eventlotterysystem.model;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link Notification}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationTest {

    private static class TestableNotification extends Notification {
        final List<String[]> sentNotifications    = new java.util.ArrayList<>();
        final List<String[]> sentAskNotifications = new java.util.ArrayList<>();
        final List<String[]> loggedNotifications  = new java.util.ArrayList<>();

        @Override
        public void sendNotification(String entrantId, String message, String eventId, String sender) {
            sentNotifications.add(new String[]{entrantId, message, eventId, sender});
        }

        @Override
        public void sendNotificationAsk(String entrantId, String message, String eventId, String sender) {
            sentAskNotifications.add(new String[]{entrantId, message, eventId, sender});
        }

        @Override
        public void logNotification(String eventId, String message) {
            loggedNotifications.add(new String[]{eventId, message});
        }
    }

    @Mock private EventRepository      mockEventRepository;
    @Mock private EntrantListFirebase  mockEntrantListFirebase;
    @Mock private ProfileManager       mockProfileManager;
    @Mock private UserProfile          mockUser;
    @Mock private Event                mockEvent;

    @Mock private FirebaseFirestore    mockFirestore;

    private MockedStatic<FirebaseFirestore> firestoreStatic;
    private MockedStatic<ProfileManager>   profileManagerStatic;
    private TestableNotification notification;

    @Before
    public void setUp() throws Exception {
        firestoreStatic      = mockStatic(FirebaseFirestore.class);
        profileManagerStatic = mockStatic(ProfileManager.class);

        firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
        profileManagerStatic.when(ProfileManager::getInstance).thenReturn(mockProfileManager);
        notification = new TestableNotification();

        injectField("eventRepository",    mockEventRepository);
        injectField("entrantListFirebase", mockEntrantListFirebase);
    }

    @After
    public void tearDown() {
        if (firestoreStatic      != null) firestoreStatic.close();
        if (profileManagerStatic != null) profileManagerStatic.close();
    }

    private void injectField(String name, Object value) throws Exception {
        java.lang.reflect.Field field = Notification.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(notification, value);
    }

    // Helpers

    private void stubOrganizerName(String firstName) {
        when(mockUser.getFirstName()).thenReturn(firstName);
        doAnswer(inv -> {
            ProfileManager.UserProfileCallBack cb = inv.getArgument(0);
            cb.onComplete(mockUser);
            return null;
        }).when(mockProfileManager).getUserProfile(any());
    }

    private void stubEventLoaded(String eventId, String eventName) {
        when(mockEvent.getTitle()).thenReturn(eventName);
        doAnswer(inv -> {
            EventRepository.SingleEventCallback cb = inv.getArgument(1);
            cb.onEventLoaded(mockEvent);
            return null;
        }).when(mockEventRepository).getEvent(eq(eventId), any());
    }

    @SuppressWarnings("unchecked")
    private void stubEntrantsByStatus(String eventId, int status, List<EntrantListEntry> entrants) {
        Task<List<EntrantListEntry>> mockTask = mock(Task.class);
        doAnswer(inv -> {
            ((OnSuccessListener<List<EntrantListEntry>>) inv.getArgument(0)).onSuccess(entrants);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());
        when(mockEntrantListFirebase.getEntrantsByStatus(eq(eventId), eq(status))).thenReturn(mockTask);
    }

    @SuppressWarnings("unchecked")
    private void stubEntrantList(List<EntrantListEntry> entrants) {
        Task<List<EntrantListEntry>> mockTask = mock(Task.class);
        doAnswer(inv -> {
            ((OnSuccessListener<List<EntrantListEntry>>) inv.getArgument(0)).onSuccess(entrants);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());
        when(mockEntrantListFirebase.getEntrantList(eq("eventF"))).thenReturn(mockTask);
    }

    private EntrantListEntry makeEntrant(String id) {
        EntrantListEntry e = mock(EntrantListEntry.class);
        when(e.getEntrantId()).thenReturn(id);
        return e;
    }

    // notifyWinner, correct message

    @Test
    public void notifyWinner() {
        stubEventLoaded("event1", "Spring Gala");

        notification.notifyWinner("entrant1", "event1");

        assertEquals(1, notification.sentNotifications.size());
        String[] sent = notification.sentNotifications.get(0);
        assertEquals("entrant1", sent[0]);
        assertTrue("Message should mention event name", sent[1].contains("Spring Gala"));
        assertEquals("event1", sent[2]);
        assertEquals("System", sent[3]);
    }

    // notifyLoser, correct message

    @Test
    public void notifyLoser() {
        stubEventLoaded("event3", "Summer Fest");

        notification.notifyLoser("entrant3", "event3");

        assertEquals(1, notification.sentNotifications.size());
        String[] sent = notification.sentNotifications.get(0);
        assertEquals("entrant3", sent[0]);
        assertTrue("Message should mention event name", sent[1].contains("Summer Fest"));
        assertTrue("Message should mention losing",    sent[1].toLowerCase().contains("lost"));
        assertEquals("event3", sent[2]);
        assertEquals("System", sent[3]);
    }


    // notifyPrivateInvite, sends with Organizer Name

    @Test
    public void notifyPrivateInvite() {
        stubOrganizerName("Alice");

        notification.notifyPrivateInvite("entrant5", "event5");

        assertEquals(1, notification.sentAskNotifications.size());
        String[] sent = notification.sentAskNotifications.get(0);
        assertEquals("entrant5", sent[0]);
        assertTrue("Message should mention organizer name",  sent[1].contains("Alice"));
        assertTrue("Message should mention private",         sent[1].toLowerCase().contains("private"));
        assertEquals("event5",  sent[2]);
        assertEquals("System",  sent[3]);
    }

    // notifyInvite sends with Organizer Name

    @Test
    public void notifyInvite() {
        stubOrganizerName("Bob");

        notification.notifyInvite("entrant7", "event7");

        assertEquals(1, notification.sentAskNotifications.size());
        String[] sent = notification.sentAskNotifications.get(0);
        assertEquals("entrant7", sent[0]);
        assertTrue("Message should mention organizer name", sent[1].contains("Bob"));
        assertEquals("event7",  sent[2]);
        assertEquals("System",  sent[3]);
    }

    // notifyOrganizerInvite send regular notification with org name

    @Test
    public void notifyOrganizerInvite() {
        stubOrganizerName("Carol");

        notification.notifyOrganizerInvite("entrant8", "event8");

        assertEquals(1, notification.sentNotifications.size());
        String[] sent = notification.sentNotifications.get(0);
        assertEquals("entrant8", sent[0]);
        assertTrue("Message should mention organizer name", sent[1].contains("Carol"));
        assertTrue("Message should mention co-organizer",   sent[1].toLowerCase().contains("co-organizer"));
        assertEquals("event8",  sent[2]);
        assertEquals("System",  sent[3]);
    }

    // notifyEntrants (Waiting, Selected, Enrolled, Cancelled)

    @Test
    public void notifyAllWaitingEntrants() {
        stubOrganizerName("Dave");
        stubEntrantsByStatus("event9", 1, Arrays.asList(makeEntrant("e1"), makeEntrant("e2")));

        notification.notifyAllWaiting("Custom message", "event9");

        assertEquals(2, notification.sentNotifications.size());
        assertEquals("e1", notification.sentNotifications.get(0)[0]);
        assertEquals("e2", notification.sentNotifications.get(1)[0]);
        assertEquals("Custom message", notification.sentNotifications.get(0)[1]);
        assertEquals("Custom message", notification.sentNotifications.get(1)[1]);
    }

    @Test
    public void notifyAllSelectedEntrants() {
        stubOrganizerName("Eve");
        stubEntrantsByStatus("eventA", 2, Collections.singletonList(makeEntrant("e3")));

        notification.notifyAllSelected("Selected msg", "eventA");

        assertEquals(1, notification.sentNotifications.size());
        assertEquals("e3", notification.sentNotifications.get(0)[0]);
    }

    @Test
    public void notifyAllEnrolledEntrants() {
        stubOrganizerName("Frank");
        stubEntrantsByStatus("eventB", 3, Collections.singletonList(makeEntrant("e4")));

        notification.notifyAllEnrolled("Enrolled msg", "eventB");

        assertEquals(1, notification.sentNotifications.size());
        assertEquals("e4", notification.sentNotifications.get(0)[0]);
    }

    @Test
    public void notifyAllCancelledEntrants() {
        stubOrganizerName("Grace");
        stubEntrantsByStatus("eventC", 4, Collections.singletonList(makeEntrant("e5")));

        notification.notifyAllCancelled("Cancelled msg", "eventC");

        assertEquals(1, notification.sentNotifications.size());
        assertEquals("e5", notification.sentNotifications.get(0)[0]);
    }

    // notifyAllEntrants

    @Test
    public void notifyAllEntrants() {
        stubEntrantList(
                Arrays.asList(makeEntrant("u1"), makeEntrant("u2"), makeEntrant("u3")));

        notification.notifyAllEntrants("Broadcast", "eventF");

        assertEquals(3, notification.sentNotifications.size());
        assertEquals("u1", notification.sentNotifications.get(0)[0]);
        assertEquals("u2", notification.sentNotifications.get(1)[0]);
        assertEquals("u3", notification.sentNotifications.get(2)[0]);
    }
}