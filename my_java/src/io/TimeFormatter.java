package src.io;

public class TimeFormatter {

    // Metodo che converte il numero di secondi in un formato leggibile
    public static String secondsToSimpleString(long delta) {
        // Definizione delle costanti per ciascuna unità di tempo
        long secondsInYear = 365 * 24 * 60 * 60; // Anni (approssimazione di 365 giorni)
        long secondsInMonth = 30 * 24 * 60 * 60; // Mesi (approssimazione di 30 giorni)
        long secondsInWeek = 7 * 24 * 60 * 60; // Settimane (7 giorni)
        long secondsInDay = 24 * 60 * 60; // Giorni
        long secondsInHour = 60 * 60; // Ore
        long secondsInMinute = 60; // Minuti

        // Calcolare i vari valori
        long years = (long) Math.floor(delta / secondsInYear);
        delta %= secondsInYear;

        long months = (long) Math.floor(delta / secondsInMonth);
        delta %= secondsInMonth;

        long weeks = (long) Math.floor(delta / secondsInWeek);
        delta %= secondsInWeek;

        long days = (long) Math.floor(delta / secondsInDay);
        delta %= secondsInDay;

        long hours = (long) Math.floor(delta / secondsInHour);
        delta %= secondsInHour;

        long minutes = (long) Math.floor(delta / secondsInMinute);
        delta %= secondsInMinute;

        long seconds = delta;

        // Costruire il risultato in base alle unità di tempo non nulle
        StringBuilder result = new StringBuilder();

        if (years > 0){     return result.append(years).append("y").toString(); } //  if(months < 1) return result.toString(); 
        if (months > 0) {   return result.append(months).append("m").toString(); } // if(weeks < 1) return result.toString(); }
        if (weeks > 0) {    return result.append(weeks).append("w").toString(); } //   if(days < 1) return result.toString(); }
        if (days > 0) {     return result.append(days).append("d").toString(); } //    if(hours < 1) return result.toString(); }
        if (hours > 0) {    return result.append(hours).append("h").toString(); } //   if(minutes < 1) return result.toString(); }
        if (minutes > 0) {  return result.append(minutes).append("min").toString(); } // if(seconds < 1) return result.toString(); }
        if (seconds > 0)    return result.append(seconds).append("s").toString();

        // Restituire la stringa formattata
        return result.toString();
    }
}