package ca.mun.outshine.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import ca.mun.outshine.model.Challenge;

public class ChallengeUtil {

    public static final String TAG = "ChallengeUtil";

    private static final String[] NAMES = {
            "CSGS Challenge",
            "CS Challenge",
            "MUN Challenge",
            "WineMocol CHallenge",
    };

    // create a random challenge
    public static Challenge getRandom() {
        Challenge challenge = new Challenge();
        Random random = new Random();

        challenge.setName(getRandomString(NAMES, random));
        challenge.setType(getRandomString(TYPES, random));
        challenge.setCreator("admin");
        challenge.setPrivacy("public");
        Date date[] = getRandomDates(random);
        // challenge.setTimeCreated(date[0].getTime());
        challenge.setTime_starts(date[0]);
        challenge.setTime_ends(date[1]);
        challenge.setCompetitors(new HashMap<String, Object>());

        return challenge;
    }

    private static final String[] TYPES = {
            "steps",
            "distance",
            "calories",
    };

    // random string generator
    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    // random date generator
    private static Date[] getRandomDates(Random random) {
        Calendar rightNow = Calendar.getInstance();
        int month = rightNow.get(Calendar.MONTH);
        int day = rightNow.get(Calendar.DATE);
        int x = random.nextInt(3); // from now until next three months
        int y = random.nextInt(30); // from now until end of the month
        month = (month + x) % 11;
        day = (day + y) % 30;
        rightNow.set(Calendar.MONTH, month);
        rightNow.set(Calendar.DATE, day);
        Calendar nextDate = (Calendar) rightNow.clone();
        nextDate.add(Calendar.DATE, +7);
        return new Date[]{rightNow.getTime(), nextDate.getTime()};
    }

    public static Date getRandomTime() {
        Random random = new Random();

        Calendar rightNow = Calendar.getInstance();
        int day = rightNow.get(Calendar.DATE);
        int hour = rightNow.get(Calendar.HOUR);
        int minute = rightNow.get(Calendar.MINUTE);

        int randDay = random.nextInt(3); // from now until three days ago
        int randHour = random.nextInt(12); // from now until 12 hours ago
        int randMinute = random.nextInt(60); // from now until 59 minutes ago

        rightNow.add(Calendar.DATE, -randDay);
        rightNow.add(Calendar.HOUR, -randHour);
        rightNow.add(Calendar.MINUTE, -randMinute);

        return rightNow.getTime();
    }

    public static int getRandomScore(int max) {
        Random random = new Random();
        return random.nextInt(max);
    }

}
