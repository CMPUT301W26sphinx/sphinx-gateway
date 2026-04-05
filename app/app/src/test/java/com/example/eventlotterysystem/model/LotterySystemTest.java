package com.example.eventlotterysystem.model;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.ProfileManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class LotterySystemTest {
    // Testable
    private static class testableLotterySystem extends LotterySystem {
        final List<String[]> winnersNotified = new ArrayList<>();
        final List<String[]> losersNotified  = new ArrayList<>();

        testableLotterySystem() { super(); }

        @Override
        protected void notifyWinner(String entrantId, String eventId) {
            winnersNotified.add(new String[]{entrantId, eventId});
        }

        @Override
        protected void notifyLoser(String entrantId, String eventId) {
            losersNotified.add(new String[]{entrantId, eventId});
        }
    }

    // Mocks
    @Mock private EntrantListFirebase  mockEntrantListFirebase;
    @Mock private EventRepository      mockEventRepository;
    @Mock private Event                mockEvent;
    @Mock private FirebaseFirestore    mockFirestore;
    @Mock private ProfileManager       mockProfileManager;

    private MockedStatic<FirebaseFirestore> firestoreStatic;
    private MockedStatic<ProfileManager>   profileManagerStatic;

    private testableLotterySystem lotterySystem;

    //Setup

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        firestoreStatic      = mockStatic(FirebaseFirestore.class);
        profileManagerStatic = mockStatic(ProfileManager.class);

        firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
        profileManagerStatic.when(ProfileManager::getInstance).thenReturn(mockProfileManager);

        // Claude AI was partially used, due to heavy usage of mockito (that honestly confused me)
        // Prompt: I'm having this error, is it possible to lead me the way to fix this (insert error code)
        // Now safe: LotterySystem -> Notification -> NotificationSystem -> ProfileManager
        // all resolve to mocks instead of real Firebase
        lotterySystem = new testableLotterySystem();

        injectField("entrantListFirebase", mockEntrantListFirebase);
        injectField("eventRepository",     mockEventRepository);
    }

    @After
    public void tearDown() {
        if (firestoreStatic      != null) firestoreStatic.close();
        if (profileManagerStatic != null) profileManagerStatic.close();
    }

    private void injectField(String name, Object value) {
        try {
            java.lang.reflect.Field f = LotterySystem.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(lotterySystem, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + name, e);
        }
    }

    //Helpers

    private void stubEvent(String eventId, int capacity) {
        when(mockEvent.getCapacity()).thenReturn(capacity);
        doAnswer(inv -> {
            EventRepository.SingleEventCallback cb = inv.getArgument(1);
            cb.onEventLoaded(mockEvent);
            return null;
        }).when(mockEventRepository).getEvent(eq(eventId), any());
    }

    private EntrantListEntry makeEntrant(String id) {
        EntrantListEntry e = mock(EntrantListEntry.class);
        when(e.getEntrantId()).thenReturn(id);
        return e;
    }

    @SuppressWarnings("unchecked")
    private void stubEntrantsByStatus(String eventId, int status, List<EntrantListEntry> entrants) {
        Task<List<EntrantListEntry>> task = mock(Task.class);
        doAnswer(inv -> {
            OnSuccessListener<List<EntrantListEntry>> listener = inv.getArgument(0);
            listener.onSuccess(new ArrayList<>(entrants)); // mutable copy — LotterySystem calls remove()
            return task;
        }).when(task).addOnSuccessListener(any());
        when(mockEntrantListFirebase.getEntrantsByStatus(eq(eventId), eq(status))).thenReturn(task);
    }
    @SuppressWarnings("unchecked")
    private Task<List<EntrantListEntry>> buildSuccessTask(List<EntrantListEntry> entrants) {
        Task<List<EntrantListEntry>> task = mock(Task.class);
        doAnswer(inv -> {
            OnSuccessListener<List<EntrantListEntry>> listener = inv.getArgument(0);
            listener.onSuccess(new ArrayList<>(entrants));
            return task;
        }).when(task).addOnSuccessListener(any());
        return task;
    }

    @SuppressWarnings("unchecked")
    private void stubUpdateStatusSuccess(String eventId) {
        Task<Void> task = mock(Task.class);
        doAnswer(inv -> {
            OnSuccessListener<Void> listener = inv.getArgument(0);
            listener.onSuccess(null);
            return task;
        }).when(task).addOnSuccessListener(any());
        doReturn(task).when(task).addOnFailureListener(any());
        when(mockEntrantListFirebase.updateStatus(eq(eventId), anyString(), anyInt()))
                .thenReturn(task);
    }

    // singularLottery

    @Test
    public void singularLottery_oneEntry() {
        stubEvent("e1", 10);
        stubEntrantsByStatus("e1", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("e1", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());
        stubEntrantsByStatus("e1", EntrantListEntry.STATUS_WAITLIST,
                Collections.singletonList(makeEntrant("u1")));
        stubUpdateStatusSuccess("e1");

        lotterySystem.singularLottery("e1");

        assertEquals(1, lotterySystem.winnersNotified.size());
        assertEquals("u1", lotterySystem.winnersNotified.get(0)[0]);
        assertEquals("e1", lotterySystem.winnersNotified.get(0)[1]);
    }

    @Test
    public void singularLottery_emptyWaitlist() {
        stubEvent("e2", 10);
        stubEntrantsByStatus("e2", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("e2", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());
        stubEntrantsByStatus("e2", EntrantListEntry.STATUS_WAITLIST,   Collections.emptyList());

        lotterySystem.singularLottery("e2");

        assertTrue(lotterySystem.winnersNotified.isEmpty());
    }

    @Test
    public void singularLottery_onlyInvitedOne() {
        stubEvent("e3", 10);
        stubEntrantsByStatus("e3", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("e3", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());
        stubEntrantsByStatus("e3", EntrantListEntry.STATUS_WAITLIST,
                Arrays.asList(makeEntrant("u1"), makeEntrant("u2"), makeEntrant("u3")));
        stubUpdateStatusSuccess("e3");

        lotterySystem.singularLottery("e3");

        assertEquals("singularLottery must invite exactly 1", 1, lotterySystem.winnersNotified.size());
    }

    // sampleLottery

    @Test
    public void sampleLottery_SampleAmount() {
        stubEvent("e4", 20);
        stubEntrantsByStatus("e4", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("e4", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());
        stubEntrantsByStatus("e4", EntrantListEntry.STATUS_WAITLIST,
                Arrays.asList(makeEntrant("a"), makeEntrant("b"),
                        makeEntrant("c"), makeEntrant("d")));
        stubUpdateStatusSuccess("e4");

        lotterySystem.sampleLottery("e4", 3);

        assertEquals(3, lotterySystem.winnersNotified.size());
    }

    @Test
    public void sampleLottery_SmallerThanSample() {
        stubEvent("e5", 20);
        stubEntrantsByStatus("e5", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("e5", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());
        stubEntrantsByStatus("e5", EntrantListEntry.STATUS_WAITLIST,
                Arrays.asList(makeEntrant("x"), makeEntrant("y")));
        stubUpdateStatusSuccess("e5");

        lotterySystem.sampleLottery("e5", 10);

        assertEquals("Can't invite more than waitlist size", 2, lotterySystem.winnersNotified.size());
    }

    // firstLottery

    @Test
    public void firstLottery_toCapacityAndNotifyLoser() {
        // capacity=2, waitlist=4
        // 2 winners, 2 losers
        stubEvent("e9", 2);
        stubEntrantsByStatus("e9", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("e9", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());

        List<EntrantListEntry> firstCall =
                Arrays.asList(makeEntrant("w1"), makeEntrant("w2"),
                        makeEntrant("w3"), makeEntrant("w4"));

        List<EntrantListEntry> secondCall =
                Arrays.asList(makeEntrant("w3"), makeEntrant("w4"));

        Task<List<EntrantListEntry>> task1 = buildSuccessTask(firstCall);
        Task<List<EntrantListEntry>> task2 = buildSuccessTask(secondCall);
        when(mockEntrantListFirebase.getEntrantsByStatus("e9", EntrantListEntry.STATUS_WAITLIST))
                .thenReturn(task1)   // first call: lottery selection
                .thenReturn(task2);  // second call: loser notification

        stubUpdateStatusSuccess("e9");

        lotterySystem.firstLottery("e9");

        assertEquals(2, lotterySystem.winnersNotified.size());
        assertEquals(2, lotterySystem.losersNotified.size());
    }

    @Test
    public void firstLottery_empty() {
        stubEvent("eA", 5);
        stubEntrantsByStatus("eA", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("eA", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());
        stubEntrantsByStatus("eA", EntrantListEntry.STATUS_WAITLIST,   Collections.emptyList());

        lotterySystem.firstLottery("eA");

        assertTrue(lotterySystem.winnersNotified.isEmpty());
        assertTrue(lotterySystem.losersNotified.isEmpty());
    }

    // redrawLottery

    @Test
    public void redrawLottery() {
        stubEvent("eB", 10);
        stubEntrantsByStatus("eB", EntrantListEntry.STATUS_INVITED,    Collections.emptyList());
        stubEntrantsByStatus("eB", EntrantListEntry.STATUS_REGISTERED, Collections.emptyList());
        stubEntrantsByStatus("eB", EntrantListEntry.STATUS_WAITLIST,
                Arrays.asList(makeEntrant("r1"), makeEntrant("r2")));
        stubUpdateStatusSuccess("eB");

        lotterySystem.redrawLottery("eB");

        assertEquals(2, lotterySystem.winnersNotified.size());
    }
}