package de.fu_berlin.inf.dpp.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.annotations.Component;

/**
 * The RosterTracker is responsible for offering a convenient access for
 * receiving roster listener changes, if one is not interested in tracking
 * whether the connection is changed.
 */
@Component(module = "net")
public class RosterTracker implements IConnectionListener {

    static final Logger log = Logger.getLogger(RosterTracker.class);

    protected Connection connection;

    protected Roster roster;

    protected DispatchingRosterListener listener = new DispatchingRosterListener();

    // public RosterTracker(Saros saros) {
    // saros.getSarosNet().addListener(this);
    // }

    public RosterTracker(XMPPConnectionService connectionService) {
        connectionService.addListener(this);
    }

    /**
     * Adds a listener to this roster. The listener will be fired anytime one or
     * more changes to the roster are pushed from the server.
     * 
     * @param rosterListener
     *            a roster listener.
     */
    public void addRosterListener(IRosterListener rosterListener) {
        listener.add(rosterListener);
    }

    /**
     * Removes a listener from this roster. The listener will be fired anytime
     * one or more changes to the roster are pushed from the server.
     * 
     * @param rosterListener
     *            a roster listener.
     */
    public void removeRosterListener(IRosterListener rosterListener) {
        listener.remove(rosterListener);
    }

    protected void prepareConnection(Connection connection) {
        this.connection = connection;
        setRoster(this.connection.getRoster());
    }

    protected void disposeConnection() {
        setRoster(null);
        this.connection = null;
    }

    @Override
    public void connectionStateChanged(Connection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTING) {
            prepareConnection(connection);
        } else if (this.connection != null
            && newState != ConnectionState.CONNECTED) {
            disposeConnection();
        }
    }

    protected void setRoster(Roster newRoster) {

        // Unregister from current roster (if set)
        if (this.roster != null) {
            this.roster.removeRosterListener(listener);
        }

        this.roster = newRoster;

        // Register to new roster (if set)
        if (this.roster != null) {
            // TODO This is too late, we might miss Roster events... reload?
            this.roster.addRosterListener(listener);

            // TODO Inform the listeners about the roster being set to null?
            listener.rosterChanged(this.roster);
        }
    }

    /**
     * Returns all currently known online presences associated with a JID or an
     * unavailable presence if the user is not online or an empty list if no
     * roster is available.
     */
    public List<Presence> getPresences(JID from) {
        if (from == null)
            throw new IllegalArgumentException("JID cannot be null");

        if (roster == null)
            return Collections.emptyList();

        final List<Presence> presences = new ArrayList<Presence>();

        final Iterator<Presence> it = roster.getPresences(from.toString());

        while (it.hasNext())
            presences.add(it.next());

        return presences;
    }

    /**
     * Returns the RQ-JIDs of all presences of the given (plain) JID which are
     * available.
     * 
     * An empty list is returned if no presence for the given JID is online.
     */
    public List<JID> getAvailablePresences(JID from) {
        if (from == null)
            throw new IllegalArgumentException("JID cannot be null");

        if (roster == null)
            return Collections.emptyList();

        List<JID> result = new ArrayList<JID>(10);
        for (Presence presence : getPresences(from)) {
            if (!presence.isAvailable())
                continue;

            String rjid = presence.getFrom();
            if (rjid == null) {
                log.error("presence.getFrom() is null");
                continue;
            }
            result.add(new JID(rjid));
        }

        return result;
    }
}