package com.android.server.net;

import android.net.NetworkIdentity;
import android.util.proto.ProtoOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

public class NetworkIdentitySet extends HashSet<NetworkIdentity> implements Comparable<NetworkIdentitySet> {
    private static final int VERSION_ADD_DEFAULT_NETWORK = 5;
    private static final int VERSION_ADD_METERED = 4;
    private static final int VERSION_ADD_NETWORK_ID = 3;
    private static final int VERSION_ADD_ROAMING = 2;
    private static final int VERSION_INIT = 1;

    public NetworkIdentitySet(DataInputStream in) throws IOException {
        int version = in.readInt();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String readOptionalString;
            boolean roaming;
            boolean z = true;
            if (version <= 1) {
                in.readInt();
            }
            int type = in.readInt();
            int subType = in.readInt();
            String subscriberId = readOptionalString(in);
            if (version >= 3) {
                readOptionalString = readOptionalString(in);
            } else {
                readOptionalString = null;
            }
            String networkId = readOptionalString;
            if (version >= 2) {
                roaming = in.readBoolean();
            } else {
                roaming = false;
            }
            boolean readBoolean = version >= 4 ? in.readBoolean() : type == 0;
            boolean metered = readBoolean;
            if (version >= 5) {
                z = in.readBoolean();
            }
            add(new NetworkIdentity(type, subType, subscriberId, networkId, roaming, metered, z));
        }
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(5);
        out.writeInt(size());
        Iterator it = iterator();
        while (it.hasNext()) {
            NetworkIdentity ident = (NetworkIdentity) it.next();
            out.writeInt(ident.getType());
            out.writeInt(ident.getSubType());
            writeOptionalString(out, ident.getSubscriberId());
            writeOptionalString(out, ident.getNetworkId());
            out.writeBoolean(ident.getRoaming());
            out.writeBoolean(ident.getMetered());
            out.writeBoolean(ident.getDefaultNetwork());
        }
    }

    public boolean isAnyMemberMetered() {
        if (isEmpty()) {
            return false;
        }
        Iterator it = iterator();
        while (it.hasNext()) {
            if (((NetworkIdentity) it.next()).getMetered()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyMemberRoaming() {
        if (isEmpty()) {
            return false;
        }
        Iterator it = iterator();
        while (it.hasNext()) {
            if (((NetworkIdentity) it.next()).getRoaming()) {
                return true;
            }
        }
        return false;
    }

    public boolean areAllMembersOnDefaultNetwork() {
        if (isEmpty()) {
            return true;
        }
        Iterator it = iterator();
        while (it.hasNext()) {
            if (!((NetworkIdentity) it.next()).getDefaultNetwork()) {
                return false;
            }
        }
        return true;
    }

    private static void writeOptionalString(DataOutputStream out, String value) throws IOException {
        if (value != null) {
            out.writeByte(1);
            out.writeUTF(value);
            return;
        }
        out.writeByte(0);
    }

    private static String readOptionalString(DataInputStream in) throws IOException {
        if (in.readByte() != (byte) 0) {
            return in.readUTF();
        }
        return null;
    }

    public int compareTo(NetworkIdentitySet another) {
        if (isEmpty()) {
            return -1;
        }
        if (another.isEmpty()) {
            return 1;
        }
        return ((NetworkIdentity) iterator().next()).compareTo((NetworkIdentity) another.iterator().next());
    }

    public void writeToProto(ProtoOutputStream proto, long tag) {
        long start = proto.start(tag);
        Iterator it = iterator();
        while (it.hasNext()) {
            ((NetworkIdentity) it.next()).writeToProto(proto, 2246267895809L);
        }
        proto.end(start);
    }
}