import {formatUTCTimeToLocalTime} from "../formatters/formatUTCDatetime";

export function resolveKudoEntryCollection(apiKudoEntryCollection) {
    return apiKudoEntryCollection.map(resolveKudoEntry);
}

export function resolveKudoEntry(apiKudoEntry) {
    return {
        username: apiKudoEntry.username,
        routeId: apiKudoEntry.routeId,
        modifier: apiKudoEntry.modifier,
        date: formatUTCTimeToLocalTime(apiKudoEntry.submissionDate)
    };
}