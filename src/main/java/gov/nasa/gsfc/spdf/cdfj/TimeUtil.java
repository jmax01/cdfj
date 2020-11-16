package gov.nasa.gsfc.spdf.cdfj;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Class TimeUtil.
 *
 * @author nand
 */
public final class TimeUtil {

    static final long[] jtimes;

    static final int[] leapSecondIds;

    static final long[] tt_times;

    static final int HIGHEST;

    static SimpleDateFormat sdf = new SimpleDateFormat("y'-'M'-'dd'T'HH:mm:ss.SSS");

    /** The Constant TT_JANUARY_1_1970. */
    public static final long TT_JANUARY_1_1970 = -946_727_957_816_000_000L;

    static final long JANUARY_1_1972 = Date.UTC(72, 0, 1, 0, 0, 0);

    static final int LAST_LEAP_SECOND_ID;
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        boolean[][] transition = new boolean[100][2];
        transition[2][0] = true;
        transition[2][1] = true;
        transition[3][1] = true;
        transition[4][1] = true;
        transition[5][1] = true;
        transition[6][1] = true;
        transition[7][1] = true;
        transition[8][1] = true;
        transition[9][1] = true;
        transition[11][0] = true;
        transition[12][0] = true;
        transition[13][0] = true;
        transition[15][0] = true;
        transition[17][1] = true;
        transition[19][1] = true;
        transition[20][1] = true;
        transition[22][0] = true;
        transition[23][0] = true;
        transition[24][0] = true;
        transition[25][1] = true;
        transition[27][0] = true;
        transition[28][1] = true;
        transition[35][1] = true;
        transition[38][1] = true;
        transition[42][0] = true;
        transition[45][0] = true;
        transition[46][1] = true;
        /*
         * try {
         * URL url = new URL(
         * "https://hpiers.obspm.fr/eoppc/bul/bulc/UTC-TAI.history");
         * URLConnection con = url.openConnection();
         * InputStream is = con.getInputStream();
         * byte[] ba = new byte[con.getContentLength()];
         * int index = 0;
         * int c;
         * while ((c = is.read()) != -1) ba[index++] = (byte)c;
         * String s = new String(ba);
         * Scanner sc = new Scanner(s);
         * CopyOnWriteArrayList lines = new CopyOnWriteArrayList();
         * while (sc.hasNextLine()) lines.add(sc.nextLine());
         * int n = lines.size() -2;
         * while (n > 0) {
         * Scanner scl = new Scanner((String)lines.get(n));
         * int year = scl.nextInt();
         * String mon = scl.next();
         * if (mon.startsWith("Jul")) transition[year-1970][0]=true;
         * if (mon.startsWith("Jan")) transition[year-1970-1][1]=true;
         * if (year == 1973) break;
         * n--;
         * }
         * System.out.println("Leap second table updated");
         * } catch (Exception ex) {
         * System.out.println("Unable to retrieve leap second table. " +
         * "Using existing table for version 3.6");
         * }
         */
        CopyOnWriteArrayList<Long> times = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Integer> ids = new CopyOnWriteArrayList<>();

        for (int i = 0; i < transition.length; i++) {

            if (transition[i][0]) {
                times.add(Date.UTC(70 + i, 5, 30, 23, 59, 59));
                ids.add(((1_970 + i) * 10_000) + 701);
            }

            if (transition[i][1]) {
                times.add(Date.UTC(70 + i, 11, 31, 23, 59, 59));
                ids.add(((1_971 + i) * 10_000) + 101);
            }

        }

        jtimes = new long[times.size()];
        tt_times = new long[times.size()];
        leapSecondIds = new int[times.size()];

        for (int i = 0; i < jtimes.length; i++) {
            jtimes[i] = times.get(i);
            leapSecondIds[i] = ids.get(i);

            try {
                tt_times[i] = tt2000(jtimes[i]);
            } catch (Throwable t) {
                System.out.println("Internal error.");
            }

        }

        HIGHEST = 1_000 * jtimes.length;
        LAST_LEAP_SECOND_ID = leapSecondIds[leapSecondIds.length - 1];
    }

    static Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private TimeUtil() {}

    /**
     * Gets the offset.
     *
     * @param l the l
     *
     * @return the offset
     * @
     */
    public static double getOffset(final long l) {

        if (l < JANUARY_1_1972) {
            throw new IllegalArgumentException("Times before January 1, 1972 are not supported at present");
        }

        double start;

        if (l < jtimes[0]) {
            start = l;
        } else {
            start = -1;
            int i = 0;

            while (i < (jtimes.length - 1)) {

                if (l < jtimes[i + 1]) {
                    start = l + ((i + 1) * 1_000);
                    break;
                }

                i++;
            }

            if (start < 0) {
                start = l + HIGHEST;
            }

        }

        return start;
    }

    /**
     * converts a Date to number of milliseconds since 1970 (corrected for
     * leap seconds.
     *
     * @param d the d
     *
     * @return the double
     */
    public static double milliSecondSince1970(final Date d) {
        return milliSecondSince1970(d.getTime());
    }

    /**
     * returns number of milliseconds since 1970 for given time ignoring
     * leap seconds.
     *
     * @param time the time
     *
     * @return the long
     */
    public static long milliSecondSince1970(final int[] time) {
        return milliSecondSince1970(time, false);
    }

    /**
     * corrects (java Date returned) number of milliseconds since 1970 for
     * leap seconds.
     *
     * @param javaMilliSecond the java milli second
     *
     * @return the double
     */
    public static double milliSecondSince1970(final long javaMilliSecond) {

        if (javaMilliSecond < JANUARY_1_1972) {
            throw new IllegalArgumentException("Times before January 1, 1972 are not supported at present");
        }

        int i = (jtimes.length - 1);

        while (i >= 0) {

            if (javaMilliSecond > jtimes[i]) {
                return javaMilliSecond + ((i + 1) * 1_000);
            }

            i--;
        }

        return javaMilliSecond;
    }

    /**
     * returns tt2000 for a Date.
     *
     * @param d the d
     *
     * @return the long
     */
    public static long tt2000(final Date d) {
        return TT_JANUARY_1_1970 + (1_000_000 * (long) milliSecondSince1970(d));
    }

    /**
     * returns tt2000 for the given time.
     *
     * @param time the time
     *
     * @return the long
     */
    public static long tt2000(final int[] time) {
        long msec = milliSecondSince1970(time, true);

        if (time.length < 6) {
            return tt2000(msec);
        }

        int adjust = (time[5] == 60) ? 1_000_000_000 : 0;

        if (time.length <= 7) {
            return adjust + tt2000(msec);
        }

        if (time.length == 8) {
            return adjust + tt2000(msec) + (time[7] * 1_000);
        }

        return adjust + tt2000(msec) + (time[7] * 1_000) + time[8];
    }

    /**
     * returns tt2000 for
     * (java Date returned) number of milliseconds since 1970.
     *
     * @param l the l
     *
     * @return the long
     */
    public static long tt2000(final long l) {
        return TT_JANUARY_1_1970 + (1_000_000 * (long) milliSecondSince1970(l));
    }

    /**
     * returns number of milliseconds since 1970 for given time, optionally
     * ignoring leap seconds
     */
    static long milliSecondSince1970(final int[] time, final boolean tt) {
        int[] t = new int[6];
        System.arraycopy(time, 0, t, 0, 3);
        t[1]--;

        for (int i = 3; i < 6; i++) {
            t[i] = 0;
        }

        int n = time.length;

        if (n >= 4) {
            t[3] = time[3];

            if (n >= 5) {
                t[4] = time[4];

                if (n >= 6) {
                    t[5] = time[5];
                }

            }

        }

        if (t[5] == 60) {

            if (!tt) {
                throw new IllegalArgumentException("second value 60 is valid for tt2000 only.");
            }

            int id = -1;

            if ((t[4] == 59) && (t[3] == 23)) {

                if ((time[2] == 30) && (time[1] == 6)) {
                    id = (time[0] * 10_000) + 701;
                } else {

                    if ((time[2] == 31) && (time[1] == 12)) {
                        id = ((1 + time[0]) * 10_000) + 101;
                    }

                }

            }

            if (id == -1) {
                throw new IllegalArgumentException("Invalid leap second time");
            }

            int adjust = 0;

            for (int i = (leapSecondIds.length - 1); i >= 0; i--) {

                if (id != leapSecondIds[i]) {
                    continue;
                }

                adjust = 1_000_000_000;
                break;
            }

            if (adjust == 0) {
                throw new IllegalArgumentException("Invalid leap second time");
            }

            t[5] = 59;
        }

        cal.clear();
        cal.set(t[0], t[1], t[2], t[3], t[4], t[5]);
        cal.set(Calendar.MILLISECOND, (n > 6) ? time[6] : 0);
        return cal.getTimeInMillis();
    }

    /**
     * The Class Validator.
     */
    public static final class Validator {

        private Validator() {}

        /**
         * Corrected if necessary.
         *
         * @param varTime the var time
         * @param leapId  the leap id
         *
         * @return the long
         * @
         */
        public static long correctedIfNecessary(final long varTime, final int leapId) {

            if (leapId == LAST_LEAP_SECOND_ID) {
                return varTime;
            }

            if (leapId < LAST_LEAP_SECOND_ID) { //
                int id = -1;

                for (int i = (leapSecondIds.length - 1); i >= 0; i--) {

                    if (leapId == leapSecondIds[i]) {
                        id = i;
                        break;
                    }

                }

                if (id < 0) {
                    throw new IllegalArgumentException("Invalid leapId");
                }

                // if (varTime < tt2000(jtimes[id + 1])) return varTime;
                if (varTime < tt_times[id + 1]) {
                    return varTime;
                }

                int i = (id + 1);

                for (; i < (jtimes.length - 1); i++) {

                    // if (varTime < tt2000(jtimes[i + 1])) break;
                    if (varTime < tt_times[i + 1]) {
                        break;
                    }

                }

                return varTime + ((i - id) * 1_000_000_000L);
            }

            if (varTime < tt_times[jtimes.length - 1]) {
                return varTime;
            }

            throw new IllegalArgumentException("Out of date Leap second table");
        }
    }
}
