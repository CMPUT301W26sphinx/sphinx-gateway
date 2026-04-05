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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link Notification}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationTest {

    // Testable subclass
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

    // Mocks

    @Mock private EventRepository      mockEventRepository;
    @Mock private EntrantListFirebase  mockEntrantListFirebase;
    @Mock private ProfileManager       mockProfileManager;
    @Mock private UserProfile          mockUser;
    @Mock private Event                mockEvent;

    // Firestore infrastructure mocks — only needed to satisfy the constructor chain
    @Mock private FirebaseFirestore    mockFirestore;
    @Mock private CollectionReference  mockCollection;
    @Mock private DocumentReference    mockDocument;

    // Held open for the entire test so any getInstance() call inside helpers is safe
    private MockedStatic<FirebaseFirestore> firestoreStatic;
    private MockedStatic<ProfileManager>   profileManagerStatic;

    private TestableNotification notification;

    // Setup / Teardown

    @Before
    public void setUp() throws Exception {
        // Stub the Firestore chain so constructors that call getInstance() don't crash
        when(mockFirestore.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);

        // Open static mocks BEFORE constructing TestableNotification
        firestoreStatic      = mockStatic(FirebaseFirestore.class);
        profileManagerStatic = mockStatic(ProfileManager.class);

        firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
        profileManagerStatic.when(ProfileManager::getInstance).thenReturn(mockProfileManager);

        // Now safe to construct — EventRepository/EntrantListFirebase won't hit real Firebase
        notification = new TestableNotification();

        // Inject our mocked repositories over the real ones created during construction
        injectField("eventRepository",    mockEventRepository);
        injectField("entrantListFirebase", mockEntrantListFirebase);
    }

    @After
    public void tearDown() {
        // Always close static mocks to avoid leaking them between tests
        if (firestoreStatic      != null) firestoreStatic.close();
        if (profileManagerStatic != null) profileManagerStatic.close();
    }

    private void injectField(String name, Object value) throws Exception {
        java.lang.reflect.Field field = Notification.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(notification, value);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    private void stubEventError(String eventId) {
        doAnswer(inv -> {
            EventRepository.SingleEventCallback cb = inv.getArgument(1);
            cb.onError(new Exception("Firebase error"));
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
    private void stubEntrantList(String eventId, List<EntrantListEntry> entrants) {
        Task<List<EntrantListEntry>> mockTask = mock(Task.class);
        doAnswer(inv -> {
            ((OnSuccessListener<List<EntrantListEntry>>) inv.getArgument(0)).onSuccess(entrants);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());
        when(mockEntrantListFirebase.getEntrantList(eq(eventId))).thenReturn(mockTask);
    }

    private EntrantListEntry makeEntrant(String id) {
        EntrantListEntry e = mock(EntrantListEntry.class);
        when(e.getEntrantId()).thenReturn(id);
        return e;
    }

    // ── notifyWinner ──────────────────────────────────────────────────────────

    @Test
    public void notifyWinner_eventLoaded_sendsCorrectMessage() {
        stubEventLoaded("event1", "Spring Gala");

        notification.notifyWinner("entrant1", "event1");

        assertEquals(1, notification.sentNotifications.size());
        String[] sent = notification.sentNotifications.get(0);
        assertEquals("entrant1", sent[0]);
        assertTrue("Message should mention event name", sent[1].contains("Spring Gala"));
        assertEquals("event1", sent[2]);
        assertEquals("System", sent[3]);
    }

    // ── notifyLoser ───────────────────────────────────────────────────────────

    @Test
    public void notifyLoser_eventLoaded_sendsCorrectMessage() {
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


    // ── notifyPrivateInvite ───────────────────────────────────────────────────

    @Test
    public void notifyPrivateInvite_sendsAskNotificationWithOrganizerName() {
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

    @Test
    public void notifyPrivateInvite_nullOrganizerName_usesFallback() {
        stubOrganizerName(null); // getFirstName() == null → "An organizer"

        notification.notifyPrivateInvite("entrant6", "event6");

        assertEquals(1, notification.sentAskNotifications.size());
        assertTrue(notification.sentAskNotifications.get(0)[1].contains("An organizer"));
    }

    // ── notifyInvite ──────────────────────────────────────────────────────────

    @Test
    public void notifyInvite_sendsAskNotificationWithOrganizerName() {
        stubOrganizerName("Bob");

        notification.notifyInvite("entrant7", "event7");

        assertEquals(1, notification.sentAskNotifications.size());
        String[] sent = notification.sentAskNotifications.get(0);
        assertEquals("entrant7", sent[0]);
        assertTrue("Message should mention organizer name", sent[1].contains("Bob"));
        assertEquals("event7",  sent[2]);
        assertEquals("System",  sent[3]);
    }

    // ── notifyOrganizerInvite ─────────────────────────────────────────────────

    @Test
    public void notifyOrganizerInvite_sendsRegularNotificationWithOrganizerName() {
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

    // ── notifyAllWaiting / Selected / Enrolled / Cancelled ───────────────────

    @Test
    public void notifyAllWaiting_sendsToAllWaitingEntrants() {
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
    public void notifyAllSelected_sendsToAllSelectedEntrants() {
        stubOrganizerName("Eve");
        stubEntrantsByStatus("eventA", 2, Collections.singletonList(makeEntrant("e3")));

        notification.notifyAllSelected("Selected msg", "eventA");

        assertEquals(1, notification.sentNotifications.size());
        assertEquals("e3", notification.sentNotifications.get(0)[0]);
    }

    @Test
    public void notifyAllEnrolled_sendsToAllEnrolledEntrants() {
        stubOrganizerName("Frank");
        stubEntrantsByStatus("eventB", 3, Collections.singletonList(makeEntrant("e4")));

        notification.notifyAllEnrolled("Enrolled msg", "eventB");

        assertEquals(1, notification.sentNotifications.size());
        assertEquals("e4", notification.sentNotifications.get(0)[0]);
    }

    @Test
    public void notifyAllCancelled_sendsToAllCancelledEntrants() {
        stubOrganizerName("Grace");
        stubEntrantsByStatus("eventC", 4, Collections.singletonList(makeEntrant("e5")));

        notification.notifyAllCancelled("Cancelled msg", "eventC");

        assertEquals(1, notification.sentNotifications.size());
        assertEquals("e5", notification.sentNotifications.get(0)[0]);
    }

    @Test
    public void notifyAllWaiting_logsNotificationForEachEntrant() {
        stubOrganizerName("Hal");
        stubEntrantsByStatus("eventD", 1,
                Arrays.asList(makeEntrant("e6"), makeEntrant("e7"), makeEntrant("e8")));

        notification.notifyAllWaiting("Broadcast msg", "eventD");

        assertEquals(3, notification.loggedNotifications.size());
        for (String[] log : notification.loggedNotifications) {
            assertEquals("eventD",        log[0]);
            assertEquals("Broadcast msg", log[1]);
        }
    }

    @Test
    public void notifyAllWaiting_emptyEntrantList_sendsNothing() {
        stubOrganizerName("Ivy");
        stubEntrantsByStatus("eventE", 1, Collections.emptyList());

        notification.notifyAllWaiting("Any message", "eventE");

        assertTrue(notification.sentNotifications.isEmpty());
        assertTrue(notification.loggedNotifications.isEmpty());
    }

    // ── notifyAllEntrants ─────────────────────────────────────────────────────

    @Test
    public void notifyAllEntrants_sendsToEveryEntrantInList() {
        stubEntrantList("eventF",
                Arrays.asList(makeEntrant("u1"), makeEntrant("u2"), makeEntrant("u3")));

        notification.notifyAllEntrants("Broadcast", "eventF");

        assertEquals(3, notification.sentNotifications.size());
        assertEquals("u1", notification.sentNotifications.get(0)[0]);
        assertEquals("u2", notification.sentNotifications.get(1)[0]);
        assertEquals("u3", notification.sentNotifications.get(2)[0]);
    }

    @Test
    public void notifyAllEntrants_emptyList_sendsNothing() {
        stubEntrantList("eventG", Collections.emptyList());

        notification.notifyAllEntrants("Nothing", "eventG");

        assertTrue(notification.sentNotifications.isEmpty());
    }
}