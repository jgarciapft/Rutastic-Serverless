package resources.kudoEntries.model;

import model.KudoEntry;
import resources.model.ResponseWithFlags;

public class GetUserKudoEntriesForRouteResponse extends ResponseWithFlags<Integer> {

    private KudoEntry kudoEntry;

    public GetUserKudoEntriesForRouteResponse() {
    }

    public GetUserKudoEntriesForRouteResponse(KudoEntry kudoEntry) {
        this.kudoEntry = kudoEntry;
    }

    public KudoEntry getKudoEntry() {
        return kudoEntry;
    }

    public void setKudoEntry(KudoEntry kudoEntry) {
        this.kudoEntry = kudoEntry;
    }

    public static class Flags {
        public static final int ERROR_UNAUTHORIZED = 10;
    }
}
