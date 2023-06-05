import moment from "moment";

export function formatUTCTimeToLocalTime(utcDatetimeString) {
  return moment.utc(utcDatetimeString).local().format('DD MMMM YYYY - HH:mm');
}