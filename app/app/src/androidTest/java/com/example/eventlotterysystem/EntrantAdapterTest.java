package com.example.eventlotterysystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.eventlotterysystem.UI.adapters.EntrantAdapter;
import com.example.eventlotterysystem.model.EntrantDisplay;
import com.example.eventlotterysystem.model.EntrantListEntry;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for EntrantAdapter
 * contains:
 * - setting entrants updates the item count
 * - null list clears adapter
 * - binding sets name and email correctly
 * - email fallback when null
 * - cancel button logic correct in both cases (when it works)
 * - unknown name correct fill
 *
 */
public class EntrantAdapterTest {
    private Context context;
    private FrameLayout parent;
    private EntrantAdapter adapter;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        parent = new FrameLayout(context);
        adapter = new EntrantAdapter();
    }
    /** Test setting entrants updates the item count */
    @Test
    public void testSetEntrants_updatesItemCount() {
        EntrantDisplay entrant = new EntrantDisplay("1", "John", "Doe", "john@test.com", 1);

        adapter.setEntrants(Collections.singletonList(entrant));

        assertEquals(1, adapter.getItemCount());
    }

    /** Test null list clears adapter */
    @Test
    public void testSetEntrants_nullList() {
        adapter.setEntrants(null);

        assertEquals(0, adapter.getItemCount());
    }

    /** Test binding sets name and email correctly */
    @Test
    public void testOnBindViewHolder_setsNameAndEmail() {
        EntrantDisplay entrant = new EntrantDisplay(
                "1", "John", "Doe", "john@test.com",
                EntrantListEntry.STATUS_WAITLIST
        );

        adapter.setEntrants(Collections.singletonList(entrant));
        EntrantAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals("John Doe", holder.nameText.getText().toString());
        assertEquals("john@test.com", holder.emailText.getText().toString());
    }

    /** Test email fallback when null */
    @Test
    public void testOnBindViewHolder_nullEmail_showsFallback() {
        EntrantDisplay entrant = new EntrantDisplay(
                "1", "Jane", "Doe", null,
                EntrantListEntry.STATUS_WAITLIST
        );

        adapter.setEntrants(Collections.singletonList(entrant));
        EntrantAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals("No email", holder.emailText.getText().toString());
    }

    /** Test cancel button visible for invited entrants */
    @Test
    public void testOnBindViewHolder_invited_showsCancelButton() {
        EntrantDisplay entrant = new EntrantDisplay(
                "1", "Alice", "Brown", "alice@test.com",
                EntrantListEntry.STATUS_INVITED
        );

        adapter.setEntrants(Collections.singletonList(entrant));
        EntrantAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals(View.VISIBLE, holder.cancelButton.getVisibility());
        assertTrue(holder.cancelButton.hasOnClickListeners());
    }

    /** Test cancel button hidden for non-invited entrants */
    @Test
    public void testOnBindViewHolder_notInvited_hidesCancelButton() {
        EntrantDisplay entrant = new EntrantDisplay(
                "1", "Bob", "White", "bob@test.com",
                EntrantListEntry.STATUS_WAITLIST
        );

        adapter.setEntrants(Collections.singletonList(entrant));
        EntrantAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals(View.GONE, holder.cancelButton.getVisibility());
    }

    /** Test cancel button click triggers listener */
    @Test
    public void testCancelClick_callsListener() {
        EntrantDisplay entrant = new EntrantDisplay(
                "1", "Sam", "Green", "sam@test.com",
                EntrantListEntry.STATUS_INVITED
        );

        AtomicReference<EntrantDisplay> clicked = new AtomicReference<>();
        adapter.setOnCancelClickListener(clicked::set);

        adapter.setEntrants(Collections.singletonList(entrant));
        EntrantAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        holder.cancelButton.performClick();

        assertEquals(entrant, clicked.get());
    }

    /** Test clicking does nothing if listener is null */
    @Test
    public void testCancelClick_nullListener_doesNotCrash() {
        EntrantDisplay entrant = new EntrantDisplay(
                "1", "Sam", "Green", "sam@test.com",
                EntrantListEntry.STATUS_INVITED
        );

        adapter.setOnCancelClickListener(null);

        adapter.setEntrants(Collections.singletonList(entrant));
        EntrantAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        holder.cancelButton.performClick(); // should not crash
    }

    /** Edge case: unknown name is displayed correctly */
    @Test
    public void testOnBindViewHolder_unknownName() {
        EntrantDisplay entrant = new EntrantDisplay(
                "1", null, null, "test@test.com",
                EntrantListEntry.STATUS_WAITLIST
        );

        adapter.setEntrants(Collections.singletonList(entrant));
        EntrantAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Unknown", holder.nameText.getText().toString());
    }


}
