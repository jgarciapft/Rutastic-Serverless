package resources.kudoEntries.model;

import model.KudoEntry;
import resources.model.ResponseWithFlags;

import java.util.List;

public class GetUserKudoEntriesResponse extends ResponseWithFlags<Integer> {

    private List<KudoEntry> kudoEntriesList;

    public GetUserKudoEntriesResponse() {
    }

    public GetUserKudoEntriesResponse(List<KudoEntry> kudoEntriesList) {
        this.kudoEntriesList = kudoEntriesList;
    }

    public List<KudoEntry> getKudoEntriesList() {
        return kudoEntriesList;
    }

    public void setKudoEntriesList(List<KudoEntry> kudoEntriesList) {
        this.kudoEntriesList = kudoEntriesList;
    }

    public static class Flags {
        public static final int ERROR_UNAUTHORIZED = 10;
    }
}
