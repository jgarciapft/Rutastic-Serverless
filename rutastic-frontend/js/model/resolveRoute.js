import {formatUTCTimeToLocalTime} from "../formatters/formatUTCDatetime";

export function resolveRouteCollection(apiRouteCollection) {
    return apiRouteCollection.map(resolveRoute);
}

export function resolveRoute(apiRoute) {
    if (apiRoute === null) return null;

    return {
        id: apiRoute.id,
        createdByUser: apiRoute.createdByUser,
        title: apiRoute.title,
        description: apiRoute.description,
        distance: apiRoute.distance,
        duration: apiRoute.duration,
        elevation: apiRoute.elevation,
        creationDate: formatUTCTimeToLocalTime(apiRoute.creationDate),
        categories: apiRoute.categories,
        skillLevel: apiRoute.skillLevel,
        kudos: apiRoute.kudos,
        blocked: apiRoute.blocked,
    }
}