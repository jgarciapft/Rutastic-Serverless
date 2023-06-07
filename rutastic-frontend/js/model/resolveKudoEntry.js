import {formatUTCTimeToLocalTime} from "../formatters/formatUTCDatetime";

export function resolveKudoEntryCollection(apiKudoEntryCollection) {
    return apiKudoEntryCollection.map(resolveKudoEntry);
}

export function resolveKudoEntry(apiKudoEntry) {
    if (apiKudoEntry === null) return null;

    return {
        user: apiKudoEntry.user,
        route: apiKudoEntry.route,
        modifier: apiKudoEntry.modifier,
        date: formatUTCTimeToLocalTime(apiKudoEntry.submissionDate)
    };
}